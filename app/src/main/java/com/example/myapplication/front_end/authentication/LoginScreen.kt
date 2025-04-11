package com.example.myapplication.front_end.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R

@Composable
fun AccountSuccessfullyCreated(navController: NavController) {
    val backgroundColor = Color(26 / 255f, 77 / 255f, 46 / 255f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Account Created!",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(R.drawable.whitelogo),
            contentDescription = "Chef's Companion",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Chef's Companion",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {

                navController.navigate("signIn")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),

            ) {
            Text(text = "Let's Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,)

        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) } // Store error message or null
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) } // Store error message or null
    var passwordVisible by rememberSaveable { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        // Logo and Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_withname),
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Title
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 33.sp
                ),
            )
        }


        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle Text
        Text(
            text = "Hi! Welcome back, you’ve been missed",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )


        Spacer(modifier = Modifier.height(50.dp))

        // --- Email Input ---
        InputLabelRow(label = "Email:", errorMessage = emailError) // Use helper
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) emailError = null // Clear error on change
            },
            label = { Text("Email", color = Color(26, 77, 46)) }, // Removed hardcoded label color
            placeholder = { Text("Ex. examplechef09") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(10.dp),
            isError = emailError != null, // Check if error message exists
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Password Input ---
        InputLabelRow(label = "Password:", errorMessage = passwordError)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) passwordError = null
            },
            label = { Text("Password", color = Color(26, 77, 46)) },
            placeholder = { Text("******") },
            trailingIcon = {
                val image = if (passwordVisible)
                    R.drawable.visibility
                else R.drawable.visibility_off

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = image),
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
            ),
            shape = RoundedCornerShape(10.dp),
            isError = passwordError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Forgot Password Link
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd // Align to the end
        ) {
            Text(
                text = "Forgot Password?",
                color = Color(26, 77, 46),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    navController.navigate("forgotPassword") // Consider typed routes later
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Sign In Button
        Button(
            onClick = {
                // Basic validation
                val isEmailInvalid = email.isBlank() // Add more validation later if needed
                val isPasswordInvalid = password.isBlank()

                emailError = if (isEmailInvalid) "Email cannot be empty" else null
                passwordError = if (isPasswordInvalid) "Password cannot be empty" else null

                if (emailError == null && passwordError == null) {
                    // TODO: Implement actual Login API call here
                    // Show loading state
                    println("Attempting login for: $email")
                    // If API call is successful:
                    navController.navigate("home") {
                        // Example: Clear back stack up to login
                        // popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        // launchSingleTop = true
                        // restoreState = true
                    }
                    // Else (API call fails):
                    // passwordError = "Incorrect email or password" // Set error based on API response
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)), // Keep color
            shape = RoundedCornerShape(50.dp) // Standard rounded button
        ) {
            Text(
                text = "Sign In",
                fontSize = 18.sp,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = Color.White,
                    fontSize = 18.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // --- Social Sign-In ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(color = Color.Gray, modifier = Modifier.weight(1f), thickness = 1.dp)
            Text(
                text = "Or sign in with",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(color = Color.Gray, modifier = Modifier.weight(1f), thickness = 1.dp)
        }
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
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
        Spacer(modifier = Modifier.height(22.dp))

        // --- Sign Up Navigation ---
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
                color = Color(26, 77, 46), // Keep color
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    navController.navigate("signUp") // Consider typed routes later
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
private fun InputLabelRow(label: String, errorMessage: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


// Dummy SocialLoginButton if not defined elsewhere (adjust colors if needed)
@Composable
private fun SocialLoginButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) { // Apply size to IconButton directly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.LightGray, CircleShape)
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
