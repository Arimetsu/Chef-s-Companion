package com.example.myapplication.viewModel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.User // Make sure this matches your data class
import com.example.myapplication.front_end.authentication.isValidEmail // Keep your validation
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.lang.IllegalStateException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn

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
        data class PasswordResetEmailSent(val message: String) : AuthState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // Expose as read-only StateFlow
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // -- Para makuha yung name and Image nung user and madisplay sa HomeScreen.kt --
    val userProfile: StateFlow<User?> = authState.mapNotNull { state ->
        // Determine current user ID based on auth state or current user
        when (state) {
            is AuthState.Success -> state.userId
            else -> auth.currentUser?.uid // Check current user if state isn't Success (e.g., on app start)
        }
    }.flatMapLatest { userId -> // Switch to the new user's data flow when UID changes
        if (userId == null) {
            flowOf(null) // Emit null if no user is logged in
        } else {
            db.collection("users").document(userId)
                .snapshots() // Listen for real-time updates from Firestore
                .mapNotNull { snapshot ->
                    // Attempt to convert Firestore snapshot to User object
                    snapshot.toObject(User::class.java)
                }
                .catch { e ->
                    // Handle errors during Firestore read (e.g., permissions)
                    println("Error fetching user profile for $userId: ${e.message}")

                }
        }
    }.stateIn( // Convert the Flow to StateFlow for easier Compose observation
        scope = viewModelScope, // Use the ViewModel's scope
        started = SharingStarted.WhileSubscribed(5000), // Start when UI collects, stop 5s after
        initialValue = null // Initial value before first emission
    )

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


    // --- Password Reset ---
    fun sendPasswordResetEmail(email: String) {
        if (!isValidEmail(email)) { // Use existing validation
            _authState.value = AuthState.Error("Invalid email format.")
            return
        }
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                // Use the new state for success
                _authState.value = AuthState.PasswordResetEmailSent(
                    "Password reset email sent to $email. Please check your inbox (and spam folder)."
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email address."
                    // Add other specific exceptions if needed
                    else -> e.localizedMessage ?: "Failed to send reset email. Please try again."
                }
                _authState.value = AuthState.Error(errorMessage)
            }
        }
    }

    // Remember to add isValidEmail if it's not globally accessible
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    //GOOGLE SIGN UP OR LOG IN
    private suspend fun handleSignInWithCredential(credential: AuthCredential) {
        try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: throw IllegalStateException("Firebase user null after credential sign in")
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false

            // ---> Call the Firestore check/create function <---
            checkAndCreateUserInFirestore(firebaseUser, isNewUser)

            // Login successful!
            _authState.value = AuthState.Success(firebaseUser.uid, "Sign-in successful!")

        } catch (e: Exception) {
            // Handle potential collisions
            if (e is FirebaseAuthUserCollisionException) {
                _authState.value = AuthState.Error("An account already exists with this email address using a different sign-in method.")
            } else {
                _authState.value = AuthState.Error("Sign-in failed: ${e.localizedMessage}")
            }
        }
    }

    // --- Add this Firestore helper function back ---
    private suspend fun checkAndCreateUserInFirestore(firebaseUser: FirebaseUser, isNewUser: Boolean) {
        val userRef = db.collection("users").document(firebaseUser.uid)
        try {
            val docSnapshot: DocumentSnapshot = userRef.get().await() // Explicit type for clarity
            // ---> CORRECTED THIS LINE <---
            if (!docSnapshot.exists() || isNewUser) { // Use exists() function call
                // Create a User object (Make sure your User class has these fields)
                val newUser = User(
                    uid = firebaseUser.uid,
                    username = firebaseUser.displayName ?: "User_${firebaseUser.uid.take(6)}",
                    email = firebaseUser.email ?: "",
                    profileImageUrl = firebaseUser.photoUrl?.toString(),
                    emailVerified = firebaseUser.isEmailVerified || (firebaseUser.email != null && firebaseUser.email!!.isNotEmpty())
                )
                userRef.set(newUser).await()
                println("Firestore user profile created for ${firebaseUser.uid}")
            } else {
                // Optional: Update existing profile if needed
                val updates = mutableMapOf<String, Any?>()
                val currentImageUrl = docSnapshot.getString("profileImageUrl")
                val newImageUrl = firebaseUser.photoUrl?.toString()
                if (newImageUrl != null && newImageUrl != currentImageUrl) {
                    updates["profileImageUrl"] = newImageUrl
                }
                val currentUsername = docSnapshot.getString("username")
                if ((currentUsername == null || currentUsername.startsWith("User_")) && firebaseUser.displayName != null) {
                    updates["username"] = firebaseUser.displayName
                }
                if (updates.isNotEmpty()) {
                    userRef.update(updates).await()
                    println("Firestore user profile updated for ${firebaseUser.uid}")
                } else {
                    println("Firestore user profile already exists for ${firebaseUser.uid}")
                }
            }
        } catch (e: Exception) {
            println("Error checking/creating/updating Firestore user profile for ${firebaseUser.uid}: ${e.message}")
        }
    }


    // --- Ensure signInWithGoogleCredential calls handleSignInWithCredential ---
    fun signInWithGoogleCredential(account: GoogleSignInAccount) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val idToken = account.idToken ?: throw IllegalStateException("GoogleSignInAccount idToken is null")
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                // ---> Make sure this call is present <---
                handleSignInWithCredential(credential)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Google Sign-In failed: ${e.localizedMessage}")
            }
        }
    }
}