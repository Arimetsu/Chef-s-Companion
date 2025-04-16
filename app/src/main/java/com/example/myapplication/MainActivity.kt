package com.example.yourapp

import YourRecipeScreen
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.system.Os.link
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.R
import com.example.myapplication.components.recipegrids.Recipe
import com.example.myapplication.components.recipegrids.RecipeTag
import com.example.myapplication.front_end.*
import com.example.myapplication.front_end.authentication.AccountSuccessfullyCreated
import com.example.myapplication.front_end.authentication.CreateAccountScreen
import com.example.myapplication.front_end.authentication.EmailVerificationScreen
import com.example.myapplication.front_end.authentication.ForgotPasswordScreen
import com.example.myapplication.front_end.authentication.LoginScreen
import com.example.myapplication.front_end.authentication.NewPasswordScreen
import com.example.myapplication.front_end.authentication.PasswordChangeSuccessfullyScreen
import com.example.myapplication.front_end.authentication.VerificationScreen
import com.example.myapplication.front_end.collection.NamingCollectionScreen
import com.example.myapplication.front_end.collection.NewCollectionScreen
import com.example.myapplication.front_end.home.HomeScreen
import com.example.myapplication.front_end.home.NotificationScreen
import com.example.myapplication.front_end.recipe.add.NewRecipeScreen
import com.example.myapplication.front_end.search.InteractionSearchScreen
import com.example.myapplication.front_end.userprofile.AccountPrivacyScreen
import com.example.myapplication.front_end.userprofile.EditProfileScreen
import com.example.myapplication.front_end.userprofile.FaqsScreen
import com.example.myapplication.front_end.userprofile.PrivacyPolicyScreen
import com.example.myapplication.front_end.userprofile.PrivacyScreen
import com.example.myapplication.front_end.userprofile.TermsAndConditionsScreen
import com.example.myapplication.front_end.userprofile.UserLink
import com.example.myapplication.front_end.userprofile.UserProfile
import com.example.myapplication.front_end.userprofile.UserProfileScreen
import ui.screens.mealplan.AddMealsToMealPlanScreen
import ui.screens.mealplan.MealPlanScreen
import ui.screens.mealplan.addMealPlanScreen

val sampleUser = UserProfile(
    id = "1",
    name = "John Smith",
    username = "johnsmith",
    bio = "I love cooking and I love planting 💚 follow me on yt too\n#Chef'sDaBest #JohnCooks",
    profilePictureUrl = "https://via.placeholder.com/100",
    backgroundImageUrl = "https://via.placeholder.com/600x200",
    links = listOf(
        UserLink("YouTube", "youtube.com/@johncooks")
    ),
    postsCount = 7,
    followersCount = 90030,
    followingCount = 100
)

val sampleRecipes = listOf(
    Recipe(
        id = 1,
        name = "Canned Tuna Pasta",
        imageRes = R.drawable.tryfood,
        tags = listOf(
            RecipeTag("Lunch"),
            RecipeTag("Italian")
        ),
        rating = 4.5
    ),
    Recipe(
        id = 2,
        name = "Vegetable Stir Fry",
        imageRes = R.drawable.tryfood,
        tags = listOf(
            RecipeTag("Dinner"),
            RecipeTag("Vegan")
        ),
        rating = 4.8
    ),
    Recipe(
        id = 3,
        name = "Vegetable Stir Fry",
        imageRes = R.drawable.tryfood,
        tags = listOf(
        RecipeTag("Dinner"),
        RecipeTag("Vegan")
    ),
        rating = 9.1
)
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = ScreenNavigation.Screen.LogIn.route ) {
        composable(ScreenNavigation.Screen.SignUp.route) { CreateAccountScreen(navController) }
        composable(ScreenNavigation.Screen.EmailVerification.route) {
            EmailVerificationScreen(
                navController
            )
        }
        composable(ScreenNavigation.Screen.AccountSuccessfullyCreated.route) {
            AccountSuccessfullyCreated(
                navController
            )
        }
        composable(ScreenNavigation.Screen.LogIn.route) { LoginScreen(navController) }
        composable(ScreenNavigation.Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController
            )
        }
        composable(ScreenNavigation.Screen.Verification.route) { VerificationScreen(navController) }
        composable(ScreenNavigation.Screen.NewPassword.route) { NewPasswordScreen(navController) }
        composable(ScreenNavigation.Screen.PasswordChanged.route) {
            PasswordChangeSuccessfullyScreen(
                navController
            )
        }
        composable(ScreenNavigation.Screen.Home.route) { HomeScreen(navController) } // Pass navController
        composable(ScreenNavigation.Screen.Notification.route) { NotificationScreen(navController) }
        composable(ScreenNavigation.Screen.YourRecipes.route) { YourRecipeScreen(navController) } // Pass navController
        composable(ScreenNavigation.Screen.NewCollection.route) {
            NewCollectionScreen(
                onNavigateToNaming = {
                    navController.navigate(ScreenNavigation.Screen.NamingCollection.route)
                },
                navController = navController // Pass the navController
            )
        }
        composable(ScreenNavigation.Screen.NamingCollection.route) {
            NamingCollectionScreen(navController = navController) // Create this composable }
        }
        composable(ScreenNavigation.Screen.AddRecipe.route) { NewRecipeScreen(navController) }
        composable(ScreenNavigation.Screen.SearchRecipe.route) {
            InteractionSearchScreen(
                navController
            )
        }
        composable(ScreenNavigation.Screen.SearchResult.route) { navController }

        // Meal Plan Screens
        composable(ScreenNavigation.Screen.MealPlan.route) {
            MealPlanScreen(
                navController,
                onAddMealsToMealPlanClick = { mealType ->
                    navController.navigate(
                        ScreenNavigation.Screen.AddMealsToMealPlan.createRoute(
                            mealType
                        )
                    )
                }
            )
        }
        composable(
            ScreenNavigation.Screen.AddMealsToMealPlan.route,  // "addMealsToMealPlan/{mealType}"
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType") ?: "Unknown"
            AddMealsToMealPlanScreen(navController, mealType)
        }
        composable(ScreenNavigation.Screen.AddMealPlan.route) {
            addMealPlanScreen(
                navController,
                onAddMealPlanClick = { navController.navigate(ScreenNavigation.Screen.MealPlan.route) })
        }

        //User Profile Screens
        composable(ScreenNavigation.Screen.UserProfile.route) {
            UserProfileScreen(
                userProfile = sampleUser/* your UserProfile object here */,
                navController = navController,
                // Optional: pass these if needed, otherwise defaults will apply
                onSearchClick = {
                    navController.navigate(ScreenNavigation.Screen.SearchRecipe.route)
                },
                onNotificationsClick = {  },
                onMenuClick = { /* handle menu */ },
                onLinkClick = { link -> /* handle link click */ },
                recipes = sampleRecipes,
                onRecipeClick = { recipe -> /* handle recipe click */ }
            )

        }
        composable(ScreenNavigation.Screen.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onSaveClick = {}
            )
        }
        composable(ScreenNavigation.Screen.Privacy.route) {
            PrivacyScreen(
                onBackClick = { navController.popBackStack() },
                onAccountPrivacyClick = { navController.navigate(ScreenNavigation.Screen.AccountPrivacy.route) },
                onPrivacyPolicyClick = { navController.navigate(ScreenNavigation.Screen.PrivacyPolicy.route) },
                onTermsClick = { navController.navigate(ScreenNavigation.Screen.TermsAndConditions.route) },
                onFaqsClick = { navController.navigate(ScreenNavigation.Screen.Faqs.route) }
            )
        }
        composable(ScreenNavigation.Screen.AccountPrivacy.route) {
            AccountPrivacyScreen(
                onBackClick = { navController.popBackStack() },
                onSavePasswordClick = {}
            )
        }
        composable(ScreenNavigation.Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen (
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(ScreenNavigation.Screen.TermsAndConditions.route){
            TermsAndConditionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(ScreenNavigation.Screen.Faqs.route) {
            FaqsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}








