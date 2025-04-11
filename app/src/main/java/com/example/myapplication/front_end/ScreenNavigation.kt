package com.example.myapplication.front_end

sealed class ScreenNavigation(val Route: String) {
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
        object YourRecipes : Screen("yourRecipes")
        object NewCollection : Screen("newCollection")
        object NamingCollection : Screen("namingCollection")
        object AddRecipe : Screen("addRecipe")
        object SearchRecipe : Screen("searchRecipe")

        // Meal Plan
        object MealPlan : Screen("mealPlan")
        object AddMealsToMealPlan : Screen("addMealsToMealPlan")
    }

}