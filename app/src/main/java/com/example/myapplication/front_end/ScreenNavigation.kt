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
        object CollectionDetail : Screen("collection_detail/{collectionId}") { // NEW
            fun createRoute(collectionId: String) = "collection_detail/$collectionId"
        }
        object Notification : Screen("Notification")
        object NewCollection : Screen("newCollection")

        object NamingCollection : Screen("namingCollection/{recipeIds}") { // Add argument placeholder
            fun createRoute(recipeIds: String) = "namingCollection/$recipeIds" // Helper to build route
        }
        object AddRecipe : Screen("addRecipe")
        object SearchRecipe : Screen("searchRecipe")
        object SearchResult : Screen("searchResult")


        // Meal Plan
        object MealPlan : Screen("mealPlan")
        data object AddMealsToMealPlan : Screen("addMealsToMealPlan/{mealType}") {
            fun createRoute(mealType: String) = "addMealsToMealPlan/$mealType"
        }
        object AddMealPlan : Screen("addMealPlan")

        //Clicking the Recipe Card
        object RecipeDetail : Screen("recipeDetail/{recipeId}") { // Note the argument placeholder
            fun createRoute(recipeId: String) = "recipeDetail/$recipeId"
        }

        companion object {
            fun fromRoute(route: String?): Screen? {
                return when (route?.substringBefore("/")) {
                    YourRecipes.route.substringBefore("/") -> YourRecipes
                    CollectionDetail.route.substringBefore("/") -> CollectionDetail // NEW
                    NewCollection.route.substringBefore("/") -> NewCollection
                    NamingCollection.route.substringBefore("/") -> NamingCollection
                    else -> null
                }
            }
        }

        //User Profile
        object UserProfile : Screen("userProfile")
        object EditProfile : Screen("editProfile")
        object Privacy : Screen("privacy")
        object AccountPrivacy : Screen("accountPrivacy")
        object PrivacyPolicy : Screen("privacyPolicy")
        object TermsAndConditions : Screen("termsAndConditions")
        object Faqs : Screen("faqs")
    }

}