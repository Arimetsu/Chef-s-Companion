package com.example.myapplication.viewModel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe // Ensure this matches your data class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue // Required import
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions // Required for merge updates
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

// --- Input Data Class ---
data class IngredientInput(
    val name: String = "",
    val quantity: String = "", // Keep as String for flexibility from UI
    val unit: String? = null
)

// --- ViewModel Class ---
class RecipeViewModel : ViewModel() {

    // ... (TAG, db, storage, auth, collections references remain the same) ...
    private val TAG = "RecipeViewModel"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val recipesCollection = db.collection("recipes")
    private val usersCollections = db.collection("user_collections")

    // --- State Definitions (RecipeSaveState, RecipeModifyState) remain the same ---
    sealed class RecipeSaveState {
        object Idle : RecipeSaveState()
        object Loading : RecipeSaveState()
        data class Success(val recipeId: String, val message: String = "Recipe saved successfully!") : RecipeSaveState()
        data class Error(val message: String) : RecipeSaveState()
    }
    private val _recipeSaveState = MutableStateFlow<RecipeSaveState>(RecipeSaveState.Idle)
    val recipeSaveState: StateFlow<RecipeSaveState> = _recipeSaveState.asStateFlow()

    sealed class RecipeModifyState {
        object Idle : RecipeModifyState()
        object Loading : RecipeModifyState()
        data class Success(val message: String = "Recipe updated successfully!") : RecipeModifyState()
        data class Error(val message: String) : RecipeModifyState()
    }
    private val _recipeModifyState = MutableStateFlow<RecipeModifyState>(RecipeModifyState.Idle)
    val recipeModifyState: StateFlow<RecipeModifyState> = _recipeModifyState.asStateFlow()


    // --- Function to Save NEW Recipe ---
    fun saveNewRecipe(
        context: Context,
        recipeName: String,
        imageUri: Uri?,
        selectedCuisine: String,
        selectedCategory: String,
        servings: String,
        prepTimeFormatted: String,
        cookingTimeFormatted: String,
        finalIngredients: List<IngredientInput>,
        finalInstructions: List<String>,
        personalNote: String,
        selectedCollectionId: String?,
        caloriesInput: String,
        proteinInput: String,
        fatInput: String,
        carbsInput: String
    ) {
        // ... (validation remains the same) ...
        val validationError = validateRecipeInput( recipeName, selectedCuisine, selectedCategory, servings, prepTimeFormatted, cookingTimeFormatted, finalIngredients, finalInstructions )
        if (validationError != null) { _recipeSaveState.value = RecipeSaveState.Error(validationError); Log.w(TAG, "Validation failed for new recipe: $validationError"); return }
        if (imageUri == null) { _recipeSaveState.value = RecipeSaveState.Error("Please add a recipe photo."); Log.w(TAG, "Validation failed for new recipe: Image URI null."); return }
        val currentUser = auth.currentUser; if (currentUser == null) { _recipeSaveState.value = RecipeSaveState.Error("Error: User not logged in."); Log.w(TAG, "Save new recipe failed: User not logged in."); return }

        _recipeSaveState.value = RecipeSaveState.Loading
        viewModelScope.launch {
            var uploadedImageUrl: String? = null
            var generatedRecipeId: String? = null
            try {
                uploadedImageUrl = uploadRecipeImage(context, imageUri, currentUser.uid)
                generatedRecipeId = db.collection("recipes").document().id

                val ingredientsForFirestore = mapIngredientsForFirestore(finalIngredients)

                // Prepare data as a Map to include FieldValue.serverTimestamp()
                val newRecipeData = mutableMapOf<String, Any?>(
                    "id" to generatedRecipeId,
                    "name" to recipeName.trim(),
                    "imageUrl" to uploadedImageUrl,
                    "cuisine" to selectedCuisine,
                    "category" to selectedCategory,
                    "servings" to (servings.toIntOrNull() ?: 0), // ★ Use Long
                    "prepTime" to prepTimeFormatted.trim(),
                    "cookingTime" to cookingTimeFormatted.trim(),
                    "ingredients" to ingredientsForFirestore,
                    "instructions" to finalInstructions.map { it.trim() }.filter { it.isNotEmpty() },
                    "personalNote" to personalNote.trim(),
                    "collectionId" to selectedCollectionId?.trim()?.takeIf { it.isNotEmpty() && it != "None" },
                    "authorId" to currentUser.uid,
                    "authorName" to (currentUser.displayName?.trim()?.takeIf { it.isNotEmpty() } ?: "Unknown User"),
                    "createdAt" to FieldValue.serverTimestamp(), // ★ Use server timestamp for creation
                    "calories" to caloriesInput.trim().takeIf { it.isNotBlank() },
                    "protein" to proteinInput.trim().takeIf { it.isNotBlank() },
                    "fat" to fatInput.trim().takeIf { it.isNotBlank() },
                    "carbohydrates" to carbsInput.trim().takeIf { it.isNotBlank() },
                    // Add default rating values if not handled by data class/rules
                    "averageRating" to 0.0,
                    "ratingCount" to 0L, // Use Long for count too
                    "userRatings" to emptyMap<String, Long>() // Assuming user ratings map user ID to Long rating
                )

                Log.d(TAG, "Attempting to save new recipe data to Firestore with ID: $generatedRecipeId")
                recipesCollection.document(generatedRecipeId).set(newRecipeData).await() // Set the map data
                Log.i(TAG, "New recipe document saved successfully to Firestore. Document ID: $generatedRecipeId")

                // ... (Add to collection logic remains the same) ...
                val collectionIdToUpdate = newRecipeData["collectionId"] as? String
                if (collectionIdToUpdate != null) {
                    Log.d(TAG, "Attempting to add recipe ID '$generatedRecipeId' to collection ID '$collectionIdToUpdate'")
                    try {
                        usersCollections.document(collectionIdToUpdate)
                            .update("recipeIds", FieldValue.arrayUnion(generatedRecipeId))
                            .await()
                        Log.i(TAG, "Successfully added recipe ID to collection '$collectionIdToUpdate'.")
                    } catch (collectionUpdateError: Exception) {
                        Log.e(TAG, "Failed to add recipe ID to collection '$collectionIdToUpdate'", collectionUpdateError)
                    }
                }

                _recipeSaveState.value = RecipeSaveState.Success(generatedRecipeId)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to save new recipe or upload image.", e)
                _recipeSaveState.value = RecipeSaveState.Error("Save failed: ${e.localizedMessage ?: "Unknown error"}")
                if (uploadedImageUrl != null) { viewModelScope.launch { deleteImageFromStorage(uploadedImageUrl) } }
            }
        }
    }


    // --- ★ Function to UPDATE Existing Recipe ★ ---
    fun updateRecipe(
        context: Context,
        recipeId: String,
        recipeName: String,
        newImageUri: Uri?,
        originalImageUrl: String?,
        selectedCuisine: String,
        selectedCategory: String,
        servings: String,
        prepTimeFormatted: String,
        cookingTimeFormatted: String,
        finalIngredients: List<IngredientInput>,
        finalInstructions: List<String>,
        personalNote: String,
        caloriesInput: String,
        proteinInput: String,
        fatInput: String,
        carbsInput: String
    ) {
        // ... (validation remains the same) ...
        val validationError = validateRecipeInput( recipeName, selectedCuisine, selectedCategory, servings, prepTimeFormatted, cookingTimeFormatted, finalIngredients, finalInstructions ); if (validationError != null) { _recipeModifyState.value = RecipeModifyState.Error(validationError); Log.w(TAG, "Validation failed for recipe update: $validationError"); return }
        val currentUser = auth.currentUser; if (currentUser == null) { _recipeModifyState.value = RecipeModifyState.Error("Error: User not logged in."); Log.w(TAG, "Update recipe failed: User not logged in."); return }
        if (recipeId.isBlank()) { _recipeModifyState.value = RecipeModifyState.Error("Error: Invalid Recipe ID."); Log.w(TAG, "Update recipe failed: Blank Recipe ID."); return }

        _recipeModifyState.value = RecipeModifyState.Loading
        viewModelScope.launch {
            var imageUrlForUpdate = originalImageUrl
            var uploadedUrlForCleanup: String? = null
            try {
                // 1. Handle Image Update
                if (newImageUri != null) {
                    Log.d(TAG,"New image provided for update. Uploading...")
                    val newUrl = uploadRecipeImage(context, newImageUri, currentUser.uid)
                    imageUrlForUpdate = newUrl
                    uploadedUrlForCleanup = newUrl
                    Log.d(TAG,"New image uploaded: $newUrl")
                    if (!originalImageUrl.isNullOrBlank()) {
                        viewModelScope.launch { deleteImageFromStorage(originalImageUrl) }
                    }
                }

                // 2. Prepare Update Data Map
                val recipeRef = recipesCollection.document(recipeId)
                val ingredientsForFirestore = mapIngredientsForFirestore(finalIngredients)
                val updates = mutableMapOf<String, Any?>(
                    "name" to recipeName.trim(),
                    "imageUrl" to (imageUrlForUpdate ?: ""),
                    "cuisine" to selectedCuisine,
                    "category" to selectedCategory,
                    "servings" to (servings.toIntOrNull() ?: 0),
                    "prepTime" to prepTimeFormatted.trim(),
                    "cookingTime" to cookingTimeFormatted.trim(),
                    "ingredients" to ingredientsForFirestore,
                    "instructions" to finalInstructions.map { it.trim() }.filter { it.isNotEmpty() },
                    "personalNote" to personalNote.trim(),
                    "calories" to caloriesInput.trim().takeIf { it.isNotBlank() },
                    "protein" to proteinInput.trim().takeIf { it.isNotBlank() },
                    "fat" to fatInput.trim().takeIf { it.isNotBlank() },
                    "carbohydrates" to carbsInput.trim().takeIf { it.isNotBlank() }
                    // ★ Ensure createdAt is NOT included here ★
                    // ★ Ensure rating fields are NOT included here (updated separately) ★
                    // ★ Ensure authorId/Name are NOT included here ★
                )

                Log.d(TAG, "Attempting to update Firestore document: $recipeId with data: $updates")
                recipeRef.set(updates, SetOptions.merge()).await() // Merge updates
                Log.i(TAG, "Successfully updated Firestore document: $recipeId")

                _recipeModifyState.value = RecipeModifyState.Success()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to update recipe or handle image.", e)
                _recipeModifyState.value = RecipeModifyState.Error("Update failed: ${e.localizedMessage ?: "Unknown error"}")
                if (uploadedUrlForCleanup != null) { viewModelScope.launch { deleteImageFromStorage(uploadedUrlForCleanup) } }
            }
        }
    }


    // --- Helper Functions (mapIngredientsForFirestore, validateRecipeInput, uploadRecipeImage, deleteImageFromStorage, getFileExtension) remain the same ---
    // Ensure mapIngredientsForFirestore returns List<Map<String, Any?>> as expected
    private fun mapIngredientsForFirestore(ingredients: List<IngredientInput>): List<Map<String, Any?>> {
        return ingredients
            .filter { it.name.isNotBlank() } // Ensure ingredient has a name
            .map { input ->
                val mapBuilder = mutableMapOf<String, Any>()
                mapBuilder["name"] = input.name.trim()

                val quantityTrimmed = input.quantity.trim()
                val quantityAsDouble = quantityTrimmed.toDoubleOrNull()
                if (quantityAsDouble != null) {
                    if (quantityAsDouble == quantityAsDouble.toLong().toDouble()) {
                        mapBuilder["quantity"] = quantityAsDouble.toLong() // Store as Long
                    } else {
                        mapBuilder["quantity"] = quantityAsDouble // Store as Double
                    }
                } else if (quantityTrimmed.isNotEmpty()) {
                    mapBuilder["quantity"] = quantityTrimmed // Store as String
                }

                input.unit?.trim()?.takeIf { it.isNotEmpty() }?.let { validUnit ->
                    mapBuilder["unit"] = validUnit
                }
                mapBuilder.toMap()
            }
    }

    private fun validateRecipeInput( recipeName: String, selectedCuisine: String, selectedCategory: String, servings: String, prepTimeFormatted: String, cookingTimeFormatted: String, finalIngredients: List<IngredientInput>, finalInstructions: List<String> ): String? {
        if (recipeName.isBlank()) return "Recipe Name cannot be empty."
        if (selectedCuisine.isBlank()) return "Please select a cuisine."
        if (selectedCategory.isBlank()) return "Please select a category."
        val servingsLong = servings.toLongOrNull() // Check as Long
        if (servingsLong == null || servingsLong <= 0) return "Please enter a valid number of servings (more than 0)."
        if (prepTimeFormatted.isBlank() && cookingTimeFormatted.isBlank()) return "Please enter Preparation or Cooking Time."
        if (finalIngredients.none { it.name.isNotBlank() }) return "Please add at least one valid ingredient (with a name)."
        if (finalInstructions.none { it.isNotBlank() }) return "Please add at least one instruction step."
        return null
    }

    private suspend fun uploadRecipeImage(context: Context, imageUri: Uri, userId: String): String {
        // ... (Implementation remains the same - throws exception on error)
        Log.d(TAG, "--- Entering uploadRecipeImage ---")
        val extension = getFileExtension(context, imageUri) ?: "jpg"
        val filename = "${UUID.randomUUID()}.$extension"
        val storagePath = "recipes_images/$userId/$filename"
        val storageRef = storage.reference.child(storagePath)
        Log.d(TAG, "Target Storage Path: $storagePath")

        return try {
            Log.d(TAG, "Starting putFile for $filename...")
            storageRef.putFile(imageUri).await()
            Log.d(TAG, "putFile completed for $filename.")
            val downloadUrl = storageRef.downloadUrl.await()
            Log.i(TAG, "Successfully got download URL.")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "!!! Exception during image upload !!!", e)
            throw e // Rethrow
        }
    }

    private suspend fun deleteImageFromStorage(imageUrl: String) {
        // ... (Implementation remains the same)
        if (imageUrl.isBlank() || !imageUrl.startsWith("gs://") && !imageUrl.startsWith("https://firebasestorage.googleapis.com/")) { Log.w(TAG, "Invalid or empty image URL provided for deletion: '$imageUrl'"); return }
        try {
            val imageRef = storage.getReferenceFromUrl(imageUrl); Log.d(TAG, "Attempting to delete image from Storage: ${imageRef.path}"); imageRef.delete().await(); Log.i(TAG, "Successfully deleted image: ${imageRef.path}")
        } catch (e: Exception) {
            if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) { Log.w(TAG, "Image not found for deletion (might be already deleted): $imageUrl") } else { Log.e(TAG, "Error deleting image: $imageUrl", e) }
        }
    }


    private fun getFileExtension(context: Context, uri: Uri): String? {
        // ... (Implementation remains the same)
        var extension: String? = null; try { val mimeType = context.contentResolver.getType(uri); if (mimeType != null) { extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) }; if (extension == null) { context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor -> if (cursor.moveToFirst()) { val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (nameIndex != -1) { val displayName = cursor.getString(nameIndex); if (displayName != null) { val lastDotIndex = displayName.lastIndexOf('.'); if (lastDotIndex != -1 && lastDotIndex < displayName.length - 1) { extension = displayName.substring(lastDotIndex + 1).lowercase(Locale.getDefault()) } } } } } }; } catch (e: Exception) { Log.e(TAG, "Error getting file extension for URI $uri", e); return null }; Log.d(TAG, "Determined file extension for $uri: $extension"); return extension
    }

    // --- Reset State Functions (resetRecipeSaveState, resetRecipeModifyState) remain the same ---
    fun resetRecipeSaveState() { if (_recipeSaveState.value != RecipeSaveState.Idle) { _recipeSaveState.value = RecipeSaveState.Idle } }
    fun resetRecipeModifyState() { if (_recipeModifyState.value != RecipeModifyState.Idle) { _recipeModifyState.value = RecipeModifyState.Idle } }
}