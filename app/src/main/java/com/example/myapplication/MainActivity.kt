package com.example.yourapp

import InteractionSearchScreen
import YourRecipeScreen
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.front_end.AccountSuccessfullyCreated
import com.example.myapplication.front_end.CreateAccountScreen
import com.example.myapplication.front_end.EmailVerificationScreen
import com.example.myapplication.front_end.ForgotPasswordScreen
import com.example.myapplication.front_end.*
import ui.screens.mealplan.MealPlanScreen


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

    NavHost(navController, startDestination = ScreenNavigation.Screen.Home.route ) {
        composable(ScreenNavigation.Screen.SignUp.route) { CreateAccountScreen(navController) }
        composable(ScreenNavigation.Screen.EmailVerification.route) { EmailVerificationScreen(navController) }
        composable(ScreenNavigation.Screen.AccountSuccessfullyCreated.route) { AccountSuccessfullyCreated(navController) }
        composable(ScreenNavigation.Screen.LogIn.route) { LoginScreen(navController) }
        composable(ScreenNavigation.Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(ScreenNavigation.Screen.Verification.route) { VerificationScreen(navController) }
        composable(ScreenNavigation.Screen.NewPassword.route) { NewPasswordScreen(navController) }
        composable(ScreenNavigation.Screen.PasswordChanged.route) { PasswordChangeSuccessfullyScreen(navController) }
        composable(ScreenNavigation.Screen.Home.route) { HomeScreen(navController) } // Pass navController
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
        composable(ScreenNavigation.Screen.AddRecipe.route){NewRecipeScreen(navController)}
        composable(ScreenNavigation.Screen.SearchRecipe.route) { InteractionSearchScreen(navController) }
        // Meal Plan Screens
        composable(ScreenNavigation.Screen.MealPlan.route) {
            MealPlanScreen(
                navController,
                onAddMealsToMealPlanClick = {
                    navController.navigate(ScreenNavigation.Screen.AddMealsToMealPlan.route)
                }
            )
        }
        composable(ScreenNavigation.Screen.AddMealsToMealPlan.route) {
            { }
        }
    }
}








