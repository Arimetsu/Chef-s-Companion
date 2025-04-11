package com.example.myapplication.front_end.authentication // Ensure correct package

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

// Assume Screen sealed class is defined elsewhere
// sealed class Screen(val route: String) { ... object NewPassword: Screen("newPassword") ... }


@Composable
fun VerificationScreen(navController: NavController) {
    var code by rememberSaveable { mutableStateOf("") }
    var codeError by rememberSaveable { mutableStateOf<String?>(null) } // Null = no error
    var isResendEnabled by rememberSaveable { mutableStateOf(true) }
    var timer by rememberSaveable { mutableStateOf(0) } // Initial timer value

    // Timer logic using LaunchedEffect
    // It runs when isResendEnabled becomes false (i.e., when timer starts)
    // It cancels and restarts if isResendEnabled becomes true then false again
    LaunchedEffect(key1 = isResendEnabled) {
        if (!isResendEnabled) {
            // TODO: Add backend call here to actually resend the code
            println("Resending verification code...")

            while (timer > 0) {
                delay(1000L) // Use Long for delay
                timer -= 1
            }
            // Timer finished, enable resend button again
            isResendEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp) // Add vertical padding
            .verticalScroll(rememberScrollState()), // Make scrollable
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(50.dp))

        Text(
            text = "Verification",
            // Apply theme style, override size/weight if needed
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally) // Already centered by Column
        )

        Spacer(modifier = Modifier.height(8.dp)) // Reduced space after title

        Text(
            text = "Enter Your Verification Code",
            style = MaterialTheme.typography.bodyMedium, // Apply theme style
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp)) // Adjusted spacing

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "",
            )
            Spacer(modifier = Modifier.weight(1f))
            // Display error only if it exists
            codeError?.let { // Scope function if not null
                Text(
                    text = it, // Display the specific error message
                    color = MaterialTheme.colorScheme.error, // Use theme error color
                    style = MaterialTheme.typography.labelMedium, // Use theme typography
                )
            }
        }
        // --- Verification Code Input ---
        // No label text needed here, just display potential error below
        OutlinedTextField(
            value = code,
            onValueChange = { newCode ->
                // Allow only digits and limit length (e.g., 6 digits)
                if (newCode.all { it.isDigit() } && newCode.length <= 6) {
                    code = newCode
                    if (codeError != null) codeError = null // Clear error on change
                }
            },
            label = { Text("Verification Code") },
            placeholder = { Text("XXXXXX") }, // Example placeholder
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                letterSpacing = 4.sp // Add letter spacing for code input look
            ),
            shape = RoundedCornerShape(10.dp),
            isError = codeError != null, // Drive error state by message
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Resend Code Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive our email? ",
                style = MaterialTheme.typography.bodySmall,
            )
            if (isResendEnabled) {
                Text(
                    text = "Resend",
                    color = Color(26, 77, 46),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        color = Color(26, 77, 46)
                    ),
                    modifier = Modifier.clickable {
                        // Logic to trigger resend (sets timer, disables button)
                        isResendEnabled = false
                        timer = 180 // Start 3-minute timer (adjust as needed)
                        // TODO: Trigger backend call in LaunchedEffect above
                    }
                )
            } else {
                // Format timer MM:SS
                val minutes = timer / 60
                val seconds = timer % 60
                val formattedTime = "%d:%02d".format(minutes, seconds) // Pad seconds with zero

                Text(
                    // Display the remaining time
                    text = "Resend in $formattedTime",
                    color = Color.Gray, // Use Gray for disabled state text
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(modifier = Modifier.height(50.dp))

        // --- Verify Button ---
        Button(
            onClick = {
                // 1. Basic Validation (e.g., check length)
                val validationError = if (code.isBlank()) {
                    "Code cannot be empty."
                } else if (code.length < 6) { // Assuming 6 digits required
                    "Code must be 6 digits."
                } else {
                    null
                }
                codeError = validationError

                if (validationError == null) {
                    // TODO: Implement API call to verify the code 'code'

                    // If API call is successful:
                    navController.navigate("newPassword") // Use typed route if available (Screen.NewPassword.route)
                    // Else (API call fails, code incorrect):
                    // codeError = "Incorrect verification code."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)), // Keep color
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = "Verify",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp,
                    color = Color.White
                ),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

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
                modifier = Modifier.clickable {
                    navController.navigate("signUp")
                }
            )
        }
    }
}
