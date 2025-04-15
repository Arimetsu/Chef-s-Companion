package com.example.myapplication.front_end.authentication

// Keep necessary imports
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background // Added for potential loading overlay
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
import androidx.compose.ui.platform.LocalFocusManager // To dismiss keyboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Preferred for collecting flows
import androidx.lifecycle.viewmodel.compose.viewModel // To get ViewModel instance
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.myapplication.R
import com.example.myapplication.front_end.ScreenNavigation
// Import your ViewModel
import com.example.myapplication.viewModel.AuthViewModel // Adjust import if needed
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// --- Login Screen ---
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel() // Get instance of AuthViewModel
) {

    // UI State for input fields
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var generalError by rememberSaveable { mutableStateOf<String?>(null) } // For ViewModel errors

    // Observe ViewModel state
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val isLoading = authState is AuthViewModel.AuthState.Loading
    val focusManager = LocalFocusManager.current // To dismiss keyboard

    // --- Handle ViewModel State Changes (Side Effects) ---
    LaunchedEffect(authState) {
        when (val state = authState) { // Use 'state' variable for easier access
            is AuthViewModel.AuthState.Success -> {
                println("Login Successful! User ID: ${state.userId}")
                // Navigate to home and clear back stack up to the start destination
                navController.navigate(ScreenNavigation.Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true // Remove login/signup screens from backstack
                    }
                    launchSingleTop = true // Avoid multiple Home instances
                }
                viewModel.resetAuthStateToIdle() // Reset state after handling
            }
            is AuthViewModel.AuthState.Error -> {
                generalError = state.message // Display the error from ViewModel
                // Don't reset state here, let the user see the error
                // viewModel.resetAuthStateToIdle() // Removed - reset on user action or success/loading
            }
            AuthViewModel.AuthState.Loading -> {
                generalError = null // Clear previous errors when loading starts
                focusManager.clearFocus() // Dismiss keyboard on loading
            }
            AuthViewModel.AuthState.Idle -> {
                // Nothing specific needed when idle within this effect
            }
            is AuthViewModel.AuthState.PasswordResetEmailSent -> {
                // This state is primarily for the ForgotPasswordScreen.
                // If it somehow appears here, just log it and reset to Idle.
                println("Auth Warning: Unexpected PasswordResetEmailSent state in LoginScreen. Resetting.")
                viewModel.resetAuthStateToIdle()
            }
        }
    }

    // ---> DEFINE googleSignInClient HERE (Before the UI elements that use it) <---
    val context = LocalContext.current // Get context using LocalContext.current
    val googleSignInClient: GoogleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Use context variable here
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Use context variable here
        GoogleSignIn.getClient(context, gso)
    }

    //-- GOOGLE SIGN UP OR LOG IN
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                // Success! Get the GoogleSignInAccount
                val account = task.getResult(ApiException::class.java)!!

                // ---> HERE: The Google Account object has the info <---
                val googleUsername = account.displayName
                val googlePhotoUrl = account.photoUrl // This is a Uri
                println("Google Sign-In Success (UI): Name=$googleUsername, Photo=$googlePhotoUrl")

                // ---> THEN: Pass the account to your ViewModel <---
                viewModel.signInWithGoogleCredential(account)

            } catch (e: ApiException) {
                // Handle Google SDK error before calling ViewModel
                println("Google Sign In Error (UI): ${e.statusCode}")
                // Maybe set generalError = "Google Sign-In failed."
                viewModel.resetAuthStateToIdle()
            }
        } else {
            // Handle cancellation or other non-OK results
            println("Google Sign In cancelled or failed (Result Code: ${result.resultCode})")
            viewModel.resetAuthStateToIdle()
        }
    }

    // --- UI Layout ---
    Box(modifier = Modifier.fillMaxSize()) { // Use Box for potential overlay
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
            Text(
                text = "Email:", // Simpler label display
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    if (generalError != null) generalError = null // Clear general error on typing
                },
                label = { Text("Email", color = Color(26, 77, 46)) },
                placeholder = { Text("Ex. examplechef09@email.com") }, // Added domain hint
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(10.dp),
                isError = generalError != null && generalError!!.contains("email", ignoreCase = true), // Basic error highlighting
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(26, 77, 46),
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red
                ),
                enabled = !isLoading // Disable when loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Password Input ---
            Text(
                text = "Password:", // Simpler label display
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    if (generalError != null) generalError = null // Clear general error on typing
                },
                label = { Text("Password", color = Color(26, 77, 46)) },
                placeholder = { Text("******") },
                trailingIcon = {
                    val image = if (passwordVisible)
                        R.drawable.visibility
                    else R.drawable.visibility_off

                    IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !isLoading) {
                        Icon(
                            painter = painterResource(id = image),
                            contentDescription = "Toggle Password Visibility"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                shape = RoundedCornerShape(10.dp),
                isError = generalError != null && generalError!!.contains("password", ignoreCase = true), // Basic error highlighting
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(26, 77, 46),
                    unfocusedBorderColor = Color.Gray,
                    errorBorderColor = Color.Red
                ),
                enabled = !isLoading // Disable when loading
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Display General Error Message (from ViewModel)
            if (generalError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = generalError!!,
                    color = MaterialTheme.colorScheme.error, // Use theme error color
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth() // Allow wrapping
                )
                Spacer(modifier = Modifier.height(4.dp)) // Space below error
            } else {
                Spacer(modifier = Modifier.height(10.dp)) // Keep spacing consistent when no error
            }


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
                    modifier = Modifier.clickable(enabled = !isLoading) { // Disable clickable when loading
                        navController.navigate(ScreenNavigation.Screen.ForgotPassword.route) // USE CONSTANT
                    }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sign In Button
            Button(
                onClick = {
                    // Call ViewModel to handle login
                    focusManager.clearFocus() // Dismiss keyboard on button press
                    viewModel.loginUser(email.trim(), password) // Trim email
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),
                shape = RoundedCornerShape(50.dp),
                enabled = !isLoading // Disable button when loading
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
                    iconResId = R.drawable.icons8_facebook_logo_192, // Make sure these drawables exist
                    contentDescription = "Facebook",
                    onClick = { /* TODO: Implement Facebook Sign-In */ },
                    enabled = !isLoading
                )
                SocialLoginButton(
                    iconResId = R.drawable.icons8_google_144, // Make sure these drawables exist
                    contentDescription = "Google",
                    onClick = {
                        if (!isLoading) {
                            generalError = null
                            println("Google Sign-In button clicked...")
                            // ---> LAUNCH A COROUTINE TO SIGN OUT FIRST <---
                            scope.launch {
                                try {
                                    println("Attempting Google Sign-Out to force account picker...")
                                    googleSignInClient.signOut().await() // Sign out from Google SDK
                                    println("Google Sign-Out successful. Launching Sign-In Intent...")
                                    // ---> THEN LAUNCH THE SIGN-IN INTENT <---
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                } catch (e: Exception) {
                                    // Log the error, but likely still proceed with sign-in attempt
                                    println("Error during Google Sign-Out (continuing with sign-in attempt): ${e.message}")
                                    // Still try to launch sign-in, as the user's intent was to sign in
                                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                                }
                            }
                        }
                    },
                    enabled = !isLoading
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
                    color = Color(26, 77, 46),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable(enabled = !isLoading) { // Disable clickable when loading
                        navController.navigate(ScreenNavigation.Screen.SignUp.route) // USE CONSTANT
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
        } // End Column

        // --- Loading Overlay ---
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)) // Semi-transparent background
                    .clickable(enabled = false, onClick = {}), // Prevent clicks behind overlay
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    } // End Box
}

// --- Helper Composables (Keep or move as needed) ---

@Composable
private fun SocialLoginButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true // Add enabled state
) {
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp), enabled = enabled) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color.LightGray, CircleShape)
                .padding(1.dp), // Padding inside border
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = contentDescription,
                modifier = Modifier.size(30.dp) // Icon size
            )
        }
    }
}


// Included AccountSuccessfullyCreated for completeness, ensure route usage is correct
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
            style = MaterialTheme.typography.bodyMedium, // Consider headlineLarge/Medium
            color = Color.White
        )

        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(R.drawable.whitelogo), // Ensure this exists
            contentDescription = "Chef's Companion Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Chef's Companion",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium, // Consider headlineSmall/Medium
            color = Color.White
        )

        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                // Navigate to Login using the constant
                navController.navigate(ScreenNavigation.Screen.LogIn.route) {
                    // Optional: Clear stack if needed, depends on where this screen is reached from
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        ) {
            Text(
                text = "Let's Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium, // Consider labelLarge
            )
        }
    }
}