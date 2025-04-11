package com.example.yourapp

import YourRecipeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.example.myapplication.front_end.search.InteractionSearchScreen
import com.example.myapplication.front_end.search.SearchResult
import com.example.myapplication.front_end.recipe.add.NewRecipeScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "yourRecipes" ) {
        composable("signUp") { CreateAccountScreen(navController) }
        composable("emailVerification") { EmailVerificationScreen(navController) }
        composable("accountSuccessfully") { AccountSuccessfullyCreated(navController) }
        composable("signIn") { LoginScreen(navController) }
        composable("forgotPassword") { ForgotPasswordScreen(navController) }
        composable("verification") { VerificationScreen(navController) }
        composable("newPassword") { NewPasswordScreen(navController) }
        composable("passwordChangeSuccessfully") { PasswordChangeSuccessfullyScreen(navController) }
        composable("home") { HomeScreen(navController) } // Pass navController
        composable("yourRecipes") { YourRecipeScreen(navController) } // Pass navController
        composable(Screen.NewCollection.name) {
            NewCollectionScreen(
                onNavigateToNaming = {
                    navController.navigate(Screen.NamingCollection.name)
                },
                navController = navController // Pass the navController
            )
        }
        composable(Screen.NamingCollection.name) {
            NamingCollectionScreen(navController = navController) // Create this composable }
        }
        composable("addRecipe"){ NewRecipeScreen(navController) }
        composable("searchRecipe") { InteractionSearchScreen(navController) }
        composable("searchResult") { SearchResult(navController) }
    }
}

enum class Screen {
    NewCollection,
    NamingCollection
}








