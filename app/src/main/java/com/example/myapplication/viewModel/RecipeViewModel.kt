package com.example.myapplication.viewModel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue // Required import
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class IngredientInput(
    val name: String = "",
    val quantity: String = "",
    val unit: String? = null
)

class RecipeViewModel : ViewModel() {

    private val TAG = "RecipeViewModel"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val usersCollections = db.collection("user_collections") // Added reference

    sealed class RecipeSaveState {
        object Idle : RecipeSaveState()
        object Loading : RecipeSaveState()
        data class Success(val recipeId: String, val message: String = "Recipe saved successfully!") : RecipeSaveState()
        data class Error(val message: String) : RecipeSaveState()
    }

    private val _recipeSaveState = MutableStateFlow<RecipeSaveState>(RecipeSaveState.Idle)
    val recipeSaveState: StateFlow<RecipeSaveState> = _recipeSaveState.asStateFlow()

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
        selectedCollectionId: String?, // Renamed parameter
        caloriesInput: String,
        proteinInput: String,
        fatInput: String,
        carbsInput: String
    ) {
        val validationError = validateRecipeInput(
            recipeName, imageUri, selectedCuisine, selectedCategory, servings,
            prepTimeFormatted, cookingTimeFormatted, finalIngredients,
            finalInstructions
        )

        if (validationError != null) {
            _recipeSaveState.value = RecipeSaveState.Error(validationError)
            Log.w(TAG, "Validation failed: $validationError")
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _recipeSaveState.value = RecipeSaveState.Error("Error: User not logged in.")
            Log.w(TAG, "Save attempt failed: User not logged in.")
            return
        }

        if (imageUri == null) {
            _recipeSaveState.value = RecipeSaveState.Error("Internal error: Image URI is null despite validation.")
            Log.e(TAG, "Save attempt failed: Image URI null post-validation.")
            return
        }

        _recipeSaveState.value = RecipeSaveState.Loading

        viewModelScope.launch {
            var imageUrl: String? = null
            var recipeId: String? = null // Keep track of ID
            try {
                imageUrl = uploadRecipeImage(context, imageUri, currentUser.uid)

                if (imageUrl == null) {
                    Log.e(TAG, "Image upload returned null. Aborting recipe save.")
                    return@launch
                }
                Log.d(TAG, "Image upload successful. URL: $imageUrl")

                recipeId = db.collection("recipes").document().id
                val servingsInt = servings.toIntOrNull() ?: 0
                val ingredientsForFirestore = mapIngredientsForFirestore(finalIngredients)
                Log.d(TAG, "Mapped ingredients for Firestore: $ingredientsForFirestore")

                val newRecipe = FirebaseRecipe(
                    id = recipeId,
                    name = recipeName.trim(),
                    imageUrl = imageUrl,
                    cuisine = selectedCuisine,
                    category = selectedCategory,
                    servings = servingsInt,
                    prepTime = prepTimeFormatted.trim(),
                    cookingTime = cookingTimeFormatted.trim(),
                    ingredients = ingredientsForFirestore,
                    instructions = finalInstructions.map { it.trim() }.filter { it.isNotEmpty() },
                    personalNote = personalNote.trim(),
                    // Store selected collection ID on recipe (optional)
                    collectionId = selectedCollectionId?.trim()?.takeIf { it.isNotEmpty() && it != "None" },
                    authorId = currentUser.uid,
                    authorName = currentUser.displayName?.trim()?.takeIf { it.isNotEmpty() } ?: "Unknown User",
                    calories = caloriesInput.trim().takeIf { it.isNotBlank() },
                    protein = proteinInput.trim().takeIf { it.isNotBlank() },
                    fat = fatInput.trim().takeIf { it.isNotBlank() },
                    carbohydrates = carbsInput.trim().takeIf { it.isNotBlank() }
                    // Rating fields use default values from FirebaseRecipe class
                )

                Log.d(TAG, "Attempting to save recipe data to Firestore with ID: $recipeId")
                db.collection("recipes").document(recipeId).set(newRecipe).await()
                Log.i(TAG, "Recipe document saved successfully to Firestore. Document ID: $recipeId")

                // Add recipe ID to the selected collection
                val collectionIdToUpdate = newRecipe.collectionId // Use ID stored on recipe object
                if (collectionIdToUpdate != null && recipeId != null) {
                    Log.d(TAG, "Attempting to add recipe ID '$recipeId' to collection ID '$collectionIdToUpdate'")
                    try {
                        val collectionRef = usersCollections.document(collectionIdToUpdate)
                        collectionRef.update("recipeIds", FieldValue.arrayUnion(recipeId)).await()
                        Log.i(TAG, "Successfully added recipe ID to collection '$collectionIdToUpdate'.")
                    } catch (collectionUpdateError: Exception) {
                        Log.e(TAG, "Failed to add recipe ID to collection '$collectionIdToUpdate'", collectionUpdateError)
                        // Proceed with success state, maybe add partial success message later
                    }
                } else {
                    Log.d(TAG, "No valid collection selected ('$selectedCollectionId') or recipe ID missing, skipping add to collection.")
                }

                _recipeSaveState.value = RecipeSaveState.Success(recipeId)

            } catch (e: StorageException) {
                Log.e(TAG, "Firebase Storage Exception during save process: ${e.message}", e)
                if (_recipeSaveState.value !is RecipeSaveState.Error) {
                    _recipeSaveState.value = RecipeSaveState.Error("Image upload failed: ${e.localizedMessage ?: "Check Logcat"}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save recipe or update collection.", e)
                _recipeSaveState.value = RecipeSaveState.Error("Failed to save recipe details: ${e.localizedMessage ?: "Unknown error"}")

                imageUrl?.let { urlToDelete ->
                    if (recipeId != null) { // Only delete if recipe save potentially started
                        viewModelScope.launch {
                            try {
                                Log.w(TAG, "Save failed, attempting to delete potentially orphaned image: $urlToDelete")
                                storage.getReferenceFromUrl(urlToDelete).delete().await()
                                Log.i(TAG, "Successfully deleted orphaned image.")
                            } catch (deleteException: Exception) {
                                Log.e(TAG, "Failed to delete orphaned image after Firestore error.", deleteException)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun mapIngredientsForFirestore(ingredients: List<IngredientInput>): List<Map<String, Any?>> {
        return ingredients
            .filter { it.name.isNotBlank() }
            .map { input ->
                val mapBuilder = mutableMapOf<String, Any>()
                mapBuilder["name"] = input.name.trim()
                val quantityTrimmed = input.quantity.trim()
                val quantityAsDouble = quantityTrimmed.toDoubleOrNull()
                if (quantityAsDouble != null) {
                    mapBuilder["quantity"] = quantityAsDouble
                } else if (quantityTrimmed.isNotEmpty()) {
                    mapBuilder["quantity"] = quantityTrimmed
                }
                input.unit?.trim()?.takeIf { it.isNotEmpty() }?.let { validUnit ->
                    mapBuilder["unit"] = validUnit
                }
                mapBuilder.toMap()
            }
    }

    private fun validateRecipeInput(
        recipeName: String,
        imageUri: Uri?,
        selectedCuisine: String,
        selectedCategory: String,
        servings: String,
        prepTimeFormatted: String,
        cookingTimeFormatted: String,
        finalIngredients: List<IngredientInput>,
        finalInstructions: List<String>
    ): String? {
        if (recipeName.isBlank()) return "Recipe Name cannot be empty."
        if (imageUri == null) return "Please add a recipe photo."
        if (selectedCuisine.isBlank()) return "Please select a cuisine."
        if (selectedCategory.isBlank()) return "Please select a category."
        val servingsInt = servings.toIntOrNull()
        if (servingsInt == null || servingsInt <= 0) return "Please enter a valid number of servings (more than 0)."
        if (prepTimeFormatted.isBlank() && cookingTimeFormatted.isBlank()) return "Please enter Preparation or Cooking Time."
        if (finalIngredients.isEmpty() || finalIngredients.all { it.name.isBlank() }) return "Please add at least one valid ingredient."
        if (finalInstructions.isEmpty() || finalInstructions.all { it.isBlank() }) return "Please add at least one instruction step."
        return null
    }

    private suspend fun uploadRecipeImage(context: Context, imageUri: Uri, userId: String): String? {
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
            Log.i(TAG, "Successfully got download URL: $downloadUrl")
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e(TAG, "!!! Firebase Storage Exception during image upload !!!", e)
            if (e is StorageException) {
                Log.e(TAG, "StorageException Code: ${e.errorCode}, HttpResultCode: ${e.httpResultCode}")
            }
            _recipeSaveState.value = RecipeSaveState.Error("Image upload failed: ${e.localizedMessage ?: "Check Logcat"}")
            null
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String? {
        var extension: String? = null
        try {
            val mimeType = context.contentResolver.getType(uri)
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            }
            if (extension == null) {
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val displayName = cursor.getString(nameIndex)
                            if (displayName != null) {
                                val lastDotIndex = displayName.lastIndexOf('.')
                                if (lastDotIndex != -1 && lastDotIndex < displayName.length - 1) {
                                    extension = displayName.substring(lastDotIndex + 1).lowercase()
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file extension for URI $uri", e)
            return null
        }
        return extension
    }

    fun resetRecipeSaveState() {
        _recipeSaveState.value = RecipeSaveState.Idle
    }
}