package com.example.myapplication.front_end // Or your actual package

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
// Import your screen composables here if needed

// Keeping the original outer sealed class structure as requested
sealed class ScreenNavigation(val Route: String) { // Route property here might be unused

    sealed class Screen(val route: String) {
        // Authentication
        object SignUp : Screen("signUp")
        object EmailVerification : Screen ("emailVerification")
        object AccountSuccessfullyCreated : Screen("accountSuccessfullyCreated")
        object LogIn : Screen("logIn")
        object ForgotPassword : Screen("forgotPassword")
        object Verification : Screen("verification")
        object NewPassword : Screen("newPassword")
        object PasswordChanged : Screen("passwordChangeSuccessfully")

        // Main App
        object Home : Screen("home")
        object YourRecipes : Screen("yourRecipes") // Consistent name

        // --- Collection Routes ---
        object NewCollection : Screen("newCollection")
        object NamingCollection : Screen("namingCollection/{recipeIds}") {
            // Expecting comma-separated IDs
            fun createRoute(recipeIds: List<String>) = "namingCollection/${recipeIds.joinToString(",")}"
        }
        // ★ Updated CollectionDetail: Takes ID and Name ★
        object CollectionDetail : Screen("collection_detail/{collectionId}/{collectionName}") {
            fun createRoute(collectionId: String, collectionName: String): String {
                // Ensure names are safe for routes if they contain special characters,
                // but for simplicity matching previous structure:
                return "collection_detail/$collectionId/$collectionName"
            }
        }
        // ★ Added CollectionEdit ★
        object CollectionEdit : Screen("collection_edit/{collectionId}") {
            fun createRoute(collectionId: String) = "collection_edit/$collectionId"
        }
        // Removed duplicate AddRecipesToCollection as it overlaps with Edit/Naming
        object AddRecipeToCollection : ScreenNavigation("add_recipe_to_collection_screen/{collectionId}") {
            fun createRoute(collectionId: String) = "add_recipe_to_collection_screen/$collectionId"
        }

        object Notification : Screen("Notification")
        object AddRecipe : Screen("addRecipe")
        object SearchRecipe : Screen("searchRecipe")
        object SearchResult : Screen("searchResult")

        // Meal Plan
        object MealPlan : Screen("mealPlan")
        object AddMealsToMealPlan : Screen("addMealsToMealPlan/{mealType}") {
            fun createRoute(mealType: String) = "addMealsToMealPlan/$mealType"
        }
        object AddMealPlan : Screen("addMealPlan")

        // Clicking the Recipe Card & Editing
        object RecipeDetail : Screen("recipeDetail/{recipeId}") {
            fun createRoute(recipeId: String) = "recipeDetail/$recipeId"
        }
        object EditRecipe : Screen("editRecipe/{recipeId}") {
            fun createRoute(recipeId: String) = "editRecipe/$recipeId"
        }


        companion object {
            // Using the same pattern as before for fromRoute
            fun fromRoute(route: String?): Screen? {
                return when (route?.substringBefore("/")) {
                    // Auth
                    SignUp.route -> SignUp
                    EmailVerification.route -> EmailVerification
                    AccountSuccessfullyCreated.route -> AccountSuccessfullyCreated
                    LogIn.route -> LogIn
                    ForgotPassword.route -> ForgotPassword
                    Verification.route -> Verification
                    NewPassword.route -> NewPassword
                    PasswordChanged.route -> PasswordChanged
                    // Main
                    Home.route -> Home
                    YourRecipes.route -> YourRecipes
                    Notification.route -> Notification
                    AddRecipe.route -> AddRecipe
                    SearchRecipe.route -> SearchRecipe
                    SearchResult.route -> SearchResult
                    // Collections
                    NewCollection.route -> NewCollection
                    NamingCollection.route.substringBefore("/") -> NamingCollection
                    CollectionDetail.route.substringBefore("/") -> CollectionDetail // ★ Added ★
                    CollectionEdit.route.substringBefore("/") -> CollectionEdit     // ★ Added ★
                    // Meal Plan
                    MealPlan.route -> MealPlan
                    AddMealsToMealPlan.route.substringBefore("/") -> AddMealsToMealPlan
                    AddMealPlan.route -> AddMealPlan
                    // Recipe
                    RecipeDetail.route.substringBefore("/") -> RecipeDetail
                    EditRecipe.route.substringBefore("/") -> EditRecipe
                    else -> null // Keep null for unhandled cases
                }
            }
        }
    }
}