package com.example.myapplication.front_end.authentication

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.latoFont
import com.example.myapplication.viewModel.AuthViewModel


sealed class Screen(val route: String) {
    object Verification : Screen("verification")
    object SignUp : Screen("signUp")

}


@Composable
fun ForgotPasswordScreen(navController: NavController,
                         viewModel: AuthViewModel = viewModel()) {
    // Use rememberSaveable to survive process death/configuration changes
    var email by rememberSaveable { mutableStateOf("") }
    // General error/success message state from ViewModel
    var feedbackMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var isError by rememberSaveable { mutableStateOf(false) } // Flag for message type

    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val isLoading = authState is AuthViewModel.AuthState.Loading
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current // For potential Toast/Snackbar

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthViewModel.AuthState.PasswordResetEmailSent -> {
                feedbackMessage = state.message
                isError = false
                email = "" // Clear email field on success
                focusManager.clearFocus()
                // Optionally show a Snackbar/Toast for non-persistent feedback
                // Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthStateToIdle() // Reset state after showing message
            }
            is AuthViewModel.AuthState.Error -> {
                feedbackMessage = state.message
                isError = true
                // Don't reset state here, let user see error
            }
            AuthViewModel.AuthState.Loading -> {
                feedbackMessage = null // Clear feedback when loading
                isError = false
                focusManager.clearFocus()
            }
            else -> {
                // For Idle or Success (Login/Register Success), potentially clear feedback
                if (state == AuthViewModel.AuthState.Idle) {
                    // feedbackMessage = null // Optional: clear message when resetting
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        // Title
        Text(
            text = "Forgot Password",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Find your account first!",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Email Label Row with Error Message
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Email:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            // Display error only if it exists

            if (feedbackMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = feedbackMessage!!,
                    color = if (isError) MaterialTheme.colorScheme.error else Color(26, 77, 46), // Green for success
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (feedbackMessage != null) { // Clear feedback on typing
                    feedbackMessage = null
                    isError = false
                    // Also reset ViewModel state if it's an error state
                    if (authState is AuthViewModel.AuthState.Error) {
                        viewModel.resetAuthStateToIdle()
                    }
                }
            },
            label = { Text("Email", color = Color(26, 77, 46)) },
            placeholder = { Text("Ex. examplechef@mail.com") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(10.dp),
            isError = isError, // Use the error flag
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            ),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Send Button
        Button(
            onClick = {
                focusManager.clearFocus()
                viewModel.sendPasswordResetEmail(email.trim()) // Call ViewModel function
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),
            shape = RoundedCornerShape(50.dp),
            enabled = !isLoading
        ) {
            Text(
                text = "Send Reset Link", // Updated text
                fontSize = 18.sp,
                color = Color.White, // Explicit white
                fontFamily = latoFont // Keep font if needed
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // "Or sign in with" Divider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.weight(1f),
                thickness = 1.dp
            )
            Text(
                text = "Or sign in with",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.weight(1f),
                thickness = 1.dp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Social Login Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                24.dp,
                Alignment.CenterHorizontally
            ), // Add space between buttons & center
            modifier = Modifier.fillMaxWidth()
        ) {
            // Use a helper composable if this repeats often
            SocialLoginButton(
                iconResId = R.drawable.icons8_facebook_logo_192,
                contentDescription = "Facebook",
                onClick = { /* TODO: Implement Facebook Sign-In */ }
            )
            SocialLoginButton(
                iconResId = R.drawable.icons8_google_144,
                contentDescription = "Google",
                onClick = { /* TODO: Implement Google Sign-In */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigate to Sign Up
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don’t have an account? ",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Sign up",
                color = Color(26, 77, 46),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { // No indication needed for clickable text generally
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
    }
}

// Helper function for basic email validation
private fun validateEmail(email: String): String? { // Returns error message or null
    if (email.isBlank()) {
        return "Email cannot be empty."
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        return "Invalid email format."
    }
    // Add other specific rules if needed
    return null // No error
}

// Helper composable for Social Login Buttons
@Composable
private fun SocialLoginButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    CircleShape
                )
                .padding(1.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}