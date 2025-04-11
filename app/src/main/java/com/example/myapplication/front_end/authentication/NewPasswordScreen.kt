package com.example.myapplication.front_end.authentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.latoFont


@Composable
fun NewPasswordScreen(navController: NavController) {

    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) } // Renamed for clarity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "New Password",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        Spacer(modifier = Modifier.height(60.dp))

        // --- New Password Input ---
        InputLabelRow(label = "Password:", errorMessage = passwordError)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { newValue ->
                password = newValue
                if(newValue.length in 8..12){
                    passwordError = null
                } else {
                    passwordError = "Password must be 8-12 characters long"
                }
                // Optionally clear confirm password error when password changes
                if (confirmPasswordError == "Passwords do not match.") confirmPasswordError = null

            },
            label = { Text("Password") },
            placeholder = { Text("******") },
            trailingIcon = {
                PasswordVisibilityIcon(
                    isVisible = passwordVisible,
                    onClick = { passwordVisible = !passwordVisible }
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy( // Use theme style
                fontFamily = latoFont, // Keep font
            ),
            shape = RoundedCornerShape(10.dp),
            isError = passwordError != null, // Drive error state by message
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors( // Keep custom colors
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Confirm Password Input ---
        InputLabelRow(label = "Confirm Password:", errorMessage = confirmPasswordError)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (confirmPasswordError != null) confirmPasswordError = null // Clear error on change
            },
            label = { Text("Confirm Password") },
            placeholder = { Text("******") },
            trailingIcon = {
                PasswordVisibilityIcon(
                    isVisible = confirmPasswordVisible,
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible }
                )
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = latoFont, // Keep font
            ),
            shape = RoundedCornerShape(10.dp),
            isError = confirmPasswordError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(48.dp))

        // --- Submit Button ---
        Button(
            onClick = {
                // Reset errors before validation
                passwordError = null
                confirmPasswordError = null

                // Perform Validation
                var hasError = false
                if (password.isBlank()) {
                    passwordError = "Password cannot be empty."
                    hasError = true
                } else {
                    // Add password complexity rules here if needed
                    // if (password.length < 8) { passwordError = "Password too short."; hasError = true }
                }

                if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Please confirm your password."
                    hasError = true
                } else if (password != confirmPassword && !hasError) { // Only check mismatch if base password is ok
                    confirmPasswordError = "Passwords do not match."
                    hasError = true
                }

                if (!hasError) {
                    // TODO: Call API to update password
                    println("Updating password...")
                    // On API success:
                    navController.navigate("passwordChangeSuccessfully") {
                        // Prevent going back to password reset flow screens
                        // popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                    // On API failure:
                    // passwordError = "Failed to update password. Try again." // Example API error
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)), // Keep color
            shape = RoundedCornerShape(50.dp) // Standard rounded button
        ) {
            Text(
                text = "Submit",
                // Apply theme style, keep overrides
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = latoFont // Keep font
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp)) // Padding at bottom
    }
}


// Helper composable for Label + Error Row (Copied from previous example, adjust font if needed)
@Composable
private fun InputLabelRow(label: String, errorMessage: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge, // Theme style
            fontWeight = FontWeight.Bold,
            fontFamily = latoFont // Keep font
        )
        Spacer(modifier = Modifier.weight(1f))
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red, // Keep color as requested
                style = MaterialTheme.typography.labelMedium, // Theme style
                fontFamily = latoFont // Keep font
            )
        }
    }
}

// Helper for password visibility icon
@Composable
private fun PasswordVisibilityIcon(isVisible: Boolean, onClick: () -> Unit) {
    val imageResId = if (isVisible) R.drawable.visibility else R.drawable.visibility_off
    val description = if (isVisible) "Hide password" else "Show password"

    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = imageResId),
            contentDescription = description
        )
    }
}

@Composable
fun PasswordChangeSuccessfullyScreen(navController: NavController) {
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
            text = "Password Changed!",
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
                //
                navController.navigate("signIn")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),

            ) {
            Text(
                text = "Sign In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
            )

        }
    }
}