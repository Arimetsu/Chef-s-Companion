package com.example.myapplication.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.User // Make sure this matches your data class
import com.example.myapplication.front_end.authentication.isValidEmail // Keep your validation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.IllegalStateException

class AuthViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // UI State
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        // Added optional message for more context
        data class Success(val userId: String, val message: String? = null) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // Expose as read-only StateFlow
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // --- Registration ---
    fun registerUser(
        username: String,
        email: String,
        password: String
    ) {
        // Validation checks...
        if (username.isBlank()) {
            _authState.value = AuthState.Error("Username cannot be empty")
            return
        }
        if (!isValidEmail(email)) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        if (password.length < 8) { // Adjust length requirement as needed
            _authState.value = AuthState.Error("Password must be at least 8 characters")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("Firebase user null after creation")
                val userId = firebaseUser.uid

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                firebaseUser.sendEmailVerification().await()

                val newUser = User(
                    uid = userId,
                    username = username,
                    email = email,
                    profileImageUrl = null,
                    emailVerified = false // Explicitly false initially
                )

                db.collection("users").document(userId).set(newUser).await()

                // Success state with a message guiding the user
                _authState.value = AuthState.Success(userId, "Registration successful! Please check your email to verify.")

            } catch (e: Exception) {
                // Provide more specific error messages
                val errorMessage = when (e) {
                    is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                        "An account already exists with this email address."
                    is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
                        "Password is too weak. Please choose a stronger one."
                    else -> e.localizedMessage ?: "Registration failed: Unknown error."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    // --- Login ---
    fun loginUser(email: String, password: String) {
        if (!isValidEmail(email) || password.isBlank()) {
            _authState.value = AuthState.Error("Please enter a valid email and password.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                    ?: throw IllegalStateException("Firebase user null after sign in")
                val userId = firebaseUser.uid

                // *** CRITICAL FIX: Reload before checking verification ***
                firebaseUser.reload().await()

                // *** Check verification status AFTER reload ***
                if (firebaseUser.isEmailVerified) {
                    // Update Firestore if needed (consider checking if already true)
                    db.collection("users").document(userId)
                        .update("emailVerified", true)
                        .await() // Consider handling potential error here too
                    _authState.value = AuthState.Success(userId, "Login successful!")
                } else {
                    // *** CRITICAL FIX: Sign out if not verified ***
                    auth.signOut()
                    _authState.value = AuthState.Error("Please verify your email address before logging in.")
                    // Optionally resend, but maybe inform user first
                    // firebaseUser.sendEmailVerification().await()
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else -> e.localizedMessage ?: "Login failed: Unknown error."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    // --- Resend Verification Email ---
    // Renamed for clarity
    fun sendVerificationEmailAgain() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _authState.value = AuthState.Error("Cannot resend email. Please log in again.")
            return
        }
        // Check cached status first - no need to resend if already verified
        if (currentUser.isEmailVerified) {
            _authState.value = AuthState.Success(currentUser.uid, "Your email is already verified.")
            return
        }

        _authState.value = AuthState.Loading // Indicate action is happening
        viewModelScope.launch {
            try {
                currentUser.sendEmailVerification().await()
                _authState.value = AuthState.Success(currentUser.uid, "Verification email resent. Please check your inbox.")
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Failed to resend verification: ${e.localizedMessage}")
            }
        }
    }

    // --- Reliable Verification Check (Used by Verification Screen) ---
    // Checks the LATEST verification status by reloading first
    // Returns true if verified, false otherwise (or if user is null/error occurs)
    suspend fun isCurrentUserEmailVerified(): Boolean {
        val currentUser = auth.currentUser ?: return false // Not logged in, not verified
        return try {
            currentUser.reload().await() // Force refresh from backend
            // Return the updated status
            currentUser.isEmailVerified
        } catch (e: Exception) {
            // Log the error, could inform user via state if needed, but return false for safety
            println("Error reloading user for verification check: ${e.message}")
            _authState.value = AuthState.Error("Could not check verification status: ${e.localizedMessage}") // Optional: notify user
            false // Assume not verified if reload fails
        }
    }


    // --- Update Firestore Status (Call AFTER confirming verification via Auth) ---
    suspend fun updateUserVerificationStatusInDb() {
        val userId = auth.currentUser?.uid ?: return // Need user ID
        // Optional double-check: Ensure Auth thinks user is verified before updating DB
        if (auth.currentUser?.isEmailVerified != true) {
            println("Warning: Attempted to update DB verification for user not verified in Auth.")
            return
        }

        try {
            db.collection("users")
                .document(userId)
                .update("emailVerified", true)
                .await()
            println("Firestore emailVerified status updated for user $userId")
        } catch (e: Exception) {
            // Log this error, but maybe don't block user flow with a critical error state
            println("Error updating Firestore verification status for user $userId: ${e.message}")
            // Optionally set a non-critical error state or log to analytics
        }
    }


    // --- Other Utility Functions ---

    // This is unreliable for core logic, use isCurrentUserEmailVerified instead
    // Keep it only if needed for non-critical UI hints
    // fun checkEmailVerifiedCached(): Boolean {
    //     return auth.currentUser?.isEmailVerified ?: false
    // }

    // Removed reloadUser() as a public Unit function - prefer the specific check function

    // Removed verifyEmailCode - not needed for standard link verification

    fun getCurrentUserEmail(): String {
        // Added null check for safety, though IllegalStateException is also valid
        return auth.currentUser?.email ?: "No email found"
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isUserLoggedIn(): Boolean {
        // Simple check if a user object exists
        return auth.currentUser != null
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle // Reset state
    }

    // Function to manually reset state, e.g., after showing an error
    fun resetAuthStateToIdle() {
        // Avoid resetting if an operation is still in progress
        if (_authState.value !is AuthState.Loading) {
            _authState.value = AuthState.Idle
        }
    }
}