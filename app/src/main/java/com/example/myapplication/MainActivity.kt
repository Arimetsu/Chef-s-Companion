package com.example.yourapp

import YourRecipeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.front_end.AccountSuccessfullyCreated
import com.example.myapplication.front_end.CreateAccountScreen
import com.example.myapplication.front_end.EmailVerificationScreen
import com.example.myapplication.front_end.ForgotPasswordScreen
import com.example.myapplication.front_end.*


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

    NavHost(navController, startDestination = "signUp" ) {
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
            NewCollectionScreen(onNavigateToNaming = {
                navController.navigate(
                    Screen.NamingCollection.name
                )
            })
        }
        composable(Screen.NamingCollection.name) {
            NamingCollectionScreen(navController = navController) // Create this composable }
        }
    }
}

enum class Screen {
    NewCollection,
    NamingCollection
}








