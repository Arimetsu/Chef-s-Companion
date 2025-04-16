package com.example.myapplication.viewModel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log // Import Android Log
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FirebaseRecipe // Your Recipe data class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException // Import StorageException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class RecipeViewModel : ViewModel() {

    // Define a TAG for logging
    private val TAG = "RecipeViewModel"

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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
        finalIngredients: List<String>,
        finalInstructions: List<String>,
        personalNote: String,
        selectedCollection: String,
        caloriesInput: String,
        proteinInput: String,
        fatInput: String,
        carbsInput: String
    ) {
        val validationError = validateRecipeInput(
            recipeName, imageUri, selectedCuisine, selectedCategory, servings,
            prepTimeFormatted, cookingTimeFormatted, finalIngredients, finalInstructions
        )

        if (validationError != null) {
            _recipeSaveState.value = RecipeSaveState.Error(validationError)
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _recipeSaveState.value = RecipeSaveState.Error("Error: User not logged in.")
            return
        }

        // Ensure imageUri is not null (already validated, but good practice)
        if (imageUri == null) {
            _recipeSaveState.value = RecipeSaveState.Error("Internal error: Image URI is null despite validation.")
            return
        }

        _recipeSaveState.value = RecipeSaveState.Loading
        Log.d(TAG, "--- Attempting to save recipe ---")
        Log.d(TAG, "User UID: ${currentUser.uid}")
        Log.d(TAG, "Image URI: $imageUri")

        viewModelScope.launch {
            try {
                // Upload Image first
                val imageUrl = uploadRecipeImage(context, imageUri, currentUser.uid)

                // Check if upload failed (uploadRecipeImage handles setting Error state)
                if (imageUrl == null) {
                    Log.e(TAG, "Image upload returned null. Aborting recipe save.")
                    // _recipeSaveState is already set to Error within uploadRecipeImage
                    return@launch
                }

                Log.d(TAG, "Image upload successful. URL: $imageUrl")

                // Proceed to save recipe data to Firestore
                val recipeId = db.collection("recipes").document().id
                val servingsInt = servings.toIntOrNull() ?: 0

                val newRecipe = FirebaseRecipe(
                    id = recipeId,
                    name = recipeName.trim(),
                    imageUrl = imageUrl, // Use the obtained URL
                    cuisine = selectedCuisine,
                    category = selectedCategory,
                    servings = servingsInt,
                    prepTime = prepTimeFormatted,
                    cookingTime = cookingTimeFormatted,
                    ingredients = finalIngredients,
                    instructions = finalInstructions,
                    personalNote = personalNote.trim(),
                    collectionId = if (selectedCollection == "None" || selectedCollection.isBlank()) null else selectedCollection,
                    authorId = currentUser.uid,
                    authorName = currentUser.displayName ?: "Unknown User",
                    calories = caloriesInput.trim().takeIf { it.isNotBlank() },
                    protein = proteinInput.trim().takeIf { it.isNotBlank() },
                    fat = fatInput.trim().takeIf { it.isNotBlank() },
                    carbohydrates = carbsInput.trim().takeIf { it.isNotBlank() }
                    // createdAt will be set by Firestore @ServerTimestamp
                )

                Log.d(TAG, "Saving recipe data to Firestore with ID: $recipeId")
                db.collection("recipes").document(recipeId).set(newRecipe).await()
                Log.i(TAG, "Recipe saved successfully to Firestore.")
                _recipeSaveState.value = RecipeSaveState.Success(recipeId)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to save recipe overall.", e)
                _recipeSaveState.value = RecipeSaveState.Error("Failed to save recipe: ${e.localizedMessage ?: "Unknown error"}")
                // Consider deleting image if Firestore save fails (more complex error handling)
            }
        }
    }

    private suspend fun uploadRecipeImage(context: Context, imageUri: Uri, userId: String): String? {
        Log.d(TAG, "--- Entering uploadRecipeImage ---")
        Log.d(TAG, "UserID for Path: $userId")
        Log.d(TAG, "Input URI: $imageUri")

        // Get File Extension
        val extension = getFileExtension(context, imageUri) ?: "jpg" // Default to jpg if type unknown
        Log.d(TAG, "Determined file extension: $extension")

        // Construct Filename
        val filename = "${UUID.randomUUID()}.$extension"
        val storagePath = "recipes_images/$userId/$filename"
        val storageRef = storage.reference.child(storagePath)

        Log.d(TAG, "Generated Filename: $filename")
        Log.d(TAG, "Target Storage Path: $storagePath")

        return try {
            Log.d(TAG, "Starting putFile for $filename...")
            val uploadTaskSnapshot = storageRef.putFile(imageUri).await()
            val bytesTransferred = uploadTaskSnapshot.bytesTransferred
            val totalBytes = uploadTaskSnapshot.totalByteCount
            Log.d(TAG, "putFile completed for $filename. Bytes: $bytesTransferred / $totalBytes")

            Log.d(TAG, "Attempting to get download URL for $filename...")
            val downloadUrl = storageRef.downloadUrl.await()
            Log.i(TAG, "Successfully got download URL: $downloadUrl")
            downloadUrl.toString()

        } catch (e: Exception) {
            Log.e(TAG, "!!! Firebase Storage Exception during image upload !!!", e)
            // Log specific StorageException details if available
            if (e is StorageException) {
                Log.e(TAG, "StorageException Code: ${e.errorCode}")
                Log.e(TAG, "StorageException HttpResultCode: ${e.httpResultCode}")
                Log.e(TAG, "StorageException IsRecoverable: ${e.isRecoverableException}")
            }
            _recipeSaveState.value = RecipeSaveState.Error("Image upload failed: ${e.localizedMessage ?: "Check Logcat for details"}")
            null // Return null on failure
        }
    }

    // Helper function to get file extension from Uri
    private fun getFileExtension(context: Context, uri: Uri): String? {
        Log.d(TAG, "Attempting to get extension for URI: $uri")
        var extension: String? = null
        try {
            // 1. Try getting MIME type from ContentResolver
            val mimeType = context.contentResolver.getType(uri)
            Log.d(TAG, "MIME Type from ContentResolver: $mimeType")
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                Log.d(TAG, "Extension from MIME Type: $extension")
            }

            // 2. If MIME type didn't yield extension, try querying DISPLAY_NAME (Fallback)
            if (extension == null) {
                Log.d(TAG, "MIME type did not provide extension, trying DISPLAY_NAME query...")
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            val displayName = cursor.getString(nameIndex)
                            Log.d(TAG, "Display Name from Cursor: $displayName")
                            if (displayName != null) {
                                val lastDotIndex = displayName.lastIndexOf('.')
                                if (lastDotIndex != -1 && lastDotIndex < displayName.length - 1) {
                                    extension = displayName.substring(lastDotIndex + 1).lowercase()
                                    Log.d(TAG, "Extension extracted from Display Name: $extension")
                                } else {
                                    Log.w(TAG, "Display Name '$displayName' does not contain a valid extension.")
                                }
                            } else {
                                Log.w(TAG, "Display Name is null in cursor.")
                            }
                        } else {
                            Log.w(TAG, "DISPLAY_NAME column index not found.")
                        }
                    } else {
                        Log.w(TAG, "Cursor is empty for DISPLAY_NAME query.")
                    }
                } ?: Log.w(TAG, "ContentResolver query returned null.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file extension for URI $uri", e)
            // Return null if any exception occurs
            return null
        }

        if (extension == null) {
            Log.w(TAG, "Could not determine file extension for URI: $uri. Will default later.")
        }
        return extension
    }


    // --- Helper function for Input Validation (Unchanged) ---
    private fun validateRecipeInput(
        recipeName: String,
        imageUri: Uri?,
        selectedCuisine: String,
        selectedCategory: String,
        servings: String,
        prepTimeFormatted: String,
        cookingTimeFormatted: String,
        finalIngredients: List<String>,
        finalInstructions: List<String>
    ): String? {
        if (recipeName.isBlank()) return "Recipe Name cannot be empty."
        if (imageUri == null) return "Please add a recipe photo." // Validation passes Uri
        if (selectedCuisine.isBlank()) return "Please select a cuisine."
        if (selectedCategory.isBlank()) return "Please select a category."
        val servingsInt = servings.toIntOrNull()
        if (servingsInt == null || servingsInt <= 0) return "Please enter a valid number of servings."
        // Allow blank time if the other is filled
        if (prepTimeFormatted.isBlank() && cookingTimeFormatted.isBlank()) return "Please enter Preparation or Cooking Time."
        if (finalIngredients.isEmpty()) return "Please add at least one ingredient."
        if (finalInstructions.isEmpty()) return "Please add at least one instruction step."
        return null // All checks passed
    }

    // --- Function to reset state (Unchanged) ---
    fun resetRecipeSaveState() {
        _recipeSaveState.value = RecipeSaveState.Idle
    }

    // --- TODO: Add functions for fetching, updating, deleting recipes later ---
}