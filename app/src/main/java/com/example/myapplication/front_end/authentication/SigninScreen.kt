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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.latoFont
import kotlinx.coroutines.delay

@Composable
fun CreateAccountScreen(navController: NavController) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isChecked by rememberSaveable { mutableStateOf(false) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) } // Renamed

    // Error states as nullable strings
    var usernameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    var termsError by rememberSaveable { mutableStateOf<String?>(null) } // For checkbox error

    var showModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_withname),
                contentDescription = "App Logo",
                modifier = Modifier.size(85.dp)
            )
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy( // Use theme style
                    fontSize = 30.sp, // Keep custom size
                    fontWeight = FontWeight.Bold,
                    fontFamily = latoFont // Keep font
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fill your information below or register with your social account.", // Added newline for better fit maybe
            style = MaterialTheme.typography.bodySmall.copy( // Use theme style
                fontSize = 10.sp, // Adjusted size slightly
                fontFamily = latoFont, // Keep font
                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Center text
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // --- Username Input ---
        InputLabelRow(label = "Username:", errorMessage = usernameError)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = username,
            onValueChange = {
                if (it.length <= 13) { // Restrict length
                    username = it
                    usernameError = null // Clear error on valid change
                } else {
                    // Optionally provide immediate feedback or just validate on submit
                    // usernameError = "Username too long (max 13)"
                }
            },
            label = { Text("Username") },
            placeholder = { Text("Ex. examplechef09") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy( // Use theme style
                fontFamily = latoFont // Keep font
            ),
            shape = RoundedCornerShape(10.dp),
            isError = usernameError != null,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Email Input ---
        InputLabelRow(label = "Email:", errorMessage = emailError)
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null // Clear error on typing
            },
            label = { Text("Email") },
            placeholder = { Text("example@gmail.com") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = latoFont),
            shape = RoundedCornerShape(10.dp),
            isError = emailError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Password Input ---
        InputLabelRow(label = "Password:", errorMessage = passwordError)

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null // Clear basic error
                // Live validation for length/complexity if desired
                if (it.length > 0 && (it.length < 8 || it.length > 12)) {
                    // passwordError = "Password must be 8-12 characters" // Example live error
                }
                if (confirmPassword.isNotEmpty()) confirmPasswordError = null // Clear mismatch error potentially
            },
            label = { Text("Password") },
            placeholder = { Text("******") },
            trailingIcon = {
                PasswordVisibilityIcon(isVisible = passwordVisible, onClick = { passwordVisible = !passwordVisible })
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = latoFont),
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

        Spacer(modifier = Modifier.height(8.dp))

        // --- Confirm Password Input ---
        InputLabelRow(label = "Confirm Password:", errorMessage = confirmPasswordError)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null // Clear error on typing
            },
            label = { Text("Confirm Password") },
            placeholder = { Text("******") },
            trailingIcon = {
                PasswordVisibilityIcon(isVisible = confirmPasswordVisible, onClick = { confirmPasswordVisible = !confirmPasswordVisible })
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = latoFont),
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

        Spacer(modifier = Modifier.height(2.dp))

        // --- Terms & Conditions Checkbox ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth() // Ensure row takes width for alignment
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    if (it) termsError = null // Clear error when checked
                },
                colors = CheckboxDefaults.colors( // Optional: customize checkbox color
                    checkedColor = Color(26, 77, 46),
                    uncheckedColor = Color.Gray
                )
            )
            Text(
                text = "Agree with ",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = latoFont)
            )
            Text(
                text = "Terms & Conditions",
                color = Color(26, 77, 46), // Keep color
                style = MaterialTheme.typography.bodyMedium.copy( // Use theme style
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFont, // Keep font
                    color = Color(26, 77, 46) // Explicit override
                ),
                modifier = Modifier.clickable { showModal = true }
            )
        }
        // Terms error message
        if (termsError != null) {
            Text(
                text = termsError!!,
                color = Color.Red, // Keep color
                style = MaterialTheme.typography.bodySmall, // Use theme style
                modifier = Modifier.padding(start = 16.dp) // Indent slightly
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Sign Up Button ---
        Button(
            onClick = {
                // Reset errors
                usernameError = null
                emailError = null
                passwordError = null
                confirmPasswordError = null
                termsError = null
                var hasError = false

                // Validate fields sequentially
                if (username.isBlank()) {
                    usernameError = "Username cannot be empty."
                    hasError = true
                } else if (username.length > 13) {
                    usernameError = "Username too long (max 13)."
                    hasError = true
                }

                if (email.isBlank()) {
                    emailError = "Email cannot be empty."
                    hasError = true
                } else if (!isValidEmail(email)) {
                    emailError = "Invalid email format."
                    hasError = true
                }

                if (password.isBlank()) {
                    passwordError = "Password cannot be empty."
                    hasError = true
                } else if (password.length < 8 || password.length > 12) {
                    passwordError = "Password must be 8-12 characters."
                    hasError = true
                }

                if (confirmPassword.isBlank()) {
                    confirmPasswordError = "Please confirm your password."
                    hasError = true
                } else if (password != confirmPassword && passwordError == null) { // Only check mismatch if base password is valid
                    confirmPasswordError = "Passwords do not match."
                    hasError = true
                }

                if (!isChecked) {
                    termsError = "You must agree to the Terms & Conditions."
                    hasError = true
                }

                if (!hasError) {
                    // TODO: Call API to create account
                    println("Creating account...")
                    // On Success:
                    navController.navigate("emailVerification") // Navigate to next step
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)), // Keep color
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = "Sign Up",
                style = MaterialTheme.typography.labelLarge.copy( // Use theme style
                    fontSize = 18.sp, // Keep size
                    color = Color.White, // Keep color
                    fontFamily = latoFont // Keep font
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Social Sign-In ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Divider(color = Color.Gray, modifier = Modifier.weight(1f), thickness = 1.dp)
            Text(
                text = "Or sign in with",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = latoFont),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(color = Color.Gray, modifier = Modifier.weight(1f), thickness = 1.dp)
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth()
        ) {
            SocialLoginButton( // Assuming helper exists
                iconResId = R.drawable.icons8_google_144,
                contentDescription = "Google",
                onClick = { /* TODO: Implement Google Sign-In */ },
            )
            SocialLoginButton(
                iconResId = R.drawable.icons8_facebook_logo_192,
                contentDescription = "Facebook",
                onClick = { /* TODO: Implement Facebook Sign-In */ }
            )
        }
        Spacer(modifier = Modifier.height(18.dp))

        // --- Navigate to Sign In ---
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = latoFont)
            )
            Text(
                text = "Sign in",
                color = Color(26, 77, 46), // Keep color
                style = MaterialTheme.typography.bodyMedium.copy( // Use theme style
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFont,
                    color = Color(26, 77, 46)
                ),
                modifier = Modifier.clickable {
                    navController.navigate("signIn")
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Show Terms & Conditions Modal
    if (showModal) {
        TermsAndConditionsDialog(
            onDismiss = { showModal = false }
        )
    }
}

// --- Terms and Conditions Dialog ---
@Composable
fun TermsAndConditionsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms & Conditions",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = latoFont),
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) { // Scrollable content
                    // Term 1
                    Text(
                        text = "1. Acceptance of Terms",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = latoFont),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "By accessing or using The Chef’s Companion (the \"Service\"), you agree to comply with these Terms & Conditions (\"T&C\"). If you do not agree, refrain from using the Service.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = latoFont),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    // TODO: Add all other terms sections here following the same pattern
                    Text(
                        text = "2. User Accounts...",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = latoFont),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "You are responsible for maintaining the confidentiality of your account...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = latoFont),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    // ... more terms ...
                }
            }
        }
    }
}

// --- Email Verification Screen (Refined) ---
@Composable
fun EmailVerificationScreen(navController: NavController) {
    var code by rememberSaveable { mutableStateOf("") }
    var codeError by rememberSaveable { mutableStateOf<String?>(null) }
    var isResendEnabled by rememberSaveable { mutableStateOf(true) }
    var timer by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(key1 = isResendEnabled) {
        if (!isResendEnabled) {
            // TODO: Add backend call here to actually resend the code
            println("Resending verification code...")
            while (timer > 0) {
                delay(1000L)
                timer -= 1
            }
            isResendEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(75.dp)
                .border(1.dp, Color(26, 77, 46), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(60.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Email Verification",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 30.sp, // Adjusted size slightly
                fontWeight = FontWeight.Bold,
                fontFamily = latoFont
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Enter the code we have sent to your email address.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp, // Adjusted size
                fontFamily = latoFont,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center // Center text
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- Code Input ---
        OutlinedTextField(
            value = code,
            onValueChange = { newCode ->
                if (newCode.all { it.isDigit() } && newCode.length <= 6) { // Assuming 6 digits
                    code = newCode
                    if (codeError != null) codeError = null
                }
            },
            label = { Text("Verification Code") },
            placeholder = { Text("XXXXXX") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = latoFont,
                letterSpacing = 4.sp
            ),
            shape = RoundedCornerShape(10.dp),
            isError = codeError != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            supportingText = {
                if (codeError != null) Text(codeError!!, color = Color.Red)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),
                unfocusedBorderColor = Color.Gray,
                errorBorderColor = Color.Red
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Resend Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive code? ",
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = latoFont)
            )
            if (isResendEnabled) {
                Text(
                    text = "Resend",
                    color = Color(26, 77, 46),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        fontFamily = latoFont,
                        color = Color(26, 77, 46)
                    ),
                    modifier = Modifier.clickable {
                        isResendEnabled = false
                        timer = 180
                        // TODO: Trigger backend call in LaunchedEffect
                    }
                )
            } else {
                val minutes = timer / 60
                val seconds = timer % 60
                val formattedTime = "%d:%02d".format(minutes, seconds)
                Text(
                    text = "Resend in $formattedTime",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = latoFont)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Verify Button ---
        Button(
            onClick = {
                codeError = if (code.length < 6) { // Basic length check
                    "Code must be 6 digits."
                } else {
                    null
                }
                if (codeError == null) {
                    // TODO: Call API to verify code
                    println("Verifying code: $code")
                    // On success:
                    navController.navigate("accountSuccessfully") {
                        // popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                    // On failure:
                    // codeError = "Incorrect code."
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),
            shape = RoundedCornerShape(50.dp)
        ) {
            Text(
                text = "Verify",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp,
                    color = Color.White,
                    fontFamily = latoFont
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- Helper Functions ---

// Reusable email validation
fun isValidEmail(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Reusable Password Visibility Icon
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

// Reusable Input Label Row
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
            fontFamily = latoFont
        )
        Spacer(modifier = Modifier.weight(1f))
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = latoFont
            )
        }
    }
}

// Reusable Social Login Button
@Composable
private fun SocialLoginButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, modifier = Modifier.size(35.dp)) {
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
                modifier = Modifier.size(25.dp)
            )
        }
    }
}
