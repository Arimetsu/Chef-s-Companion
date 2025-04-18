package com.example.myapplication // Adjust to your actual package

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.content.ActivityNotFoundException
import android.os.Build
import android.os.Bundle
import android.system.Os.link
import android.widget.Toast
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.front_end.CollectionDetailScreen
import com.example.myapplication.R
import com.example.myapplication.components.recipegrids.Recipe
import com.example.myapplication.components.recipegrids.RecipeTag
import com.example.myapplication.front_end.*
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
import com.example.myapplication.front_end.home.NotificationScreen
import com.example.myapplication.front_end.recipe.add.NewRecipeScreen
import com.example.myapplication.front_end.search.InteractionSearchScreen
import com.example.myapplication.front_end.userprofile.AccountPrivacyScreen
import com.example.myapplication.front_end.userprofile.EditProfileScreen
import com.example.myapplication.front_end.userprofile.FaqsScreen
import com.example.myapplication.front_end.userprofile.PrivacyPolicyScreen
import com.example.myapplication.front_end.userprofile.PrivacyScreen
import com.example.myapplication.front_end.userprofile.TermsAndConditionsScreen
import com.example.myapplication.front_end.userprofile.UserLink
import com.example.myapplication.front_end.userprofile.UserProfile
import com.example.myapplication.front_end.userprofile.UserProfileScreen
import ui.screens.mealplan.AddMealsToMealPlanScreen
import ui.screens.mealplan.MealPlanScreen
import ui.screens.mealplan.addMealPlanScreen
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.YourRecipeScreen
import com.example.myapplication.front_end.authentication.*
import com.example.myapplication.front_end.collection.*
import com.example.myapplication.front_end.home.*
import com.example.myapplication.front_end.recipe.add.*
import com.example.myapplication.front_end.recipe.detail.RecipeDetailScreen
import com.example.myapplication.front_end.recipe.edit.EditRecipeScreen
import com.example.myapplication.front_end.search.*
import com.example.myapplication.viewModel.RecipeDetailViewModel
import com.example.myapplication.viewModel.RecipeDetailState
import com.example.myapplication.viewModel.RecipeUpdateState
import ui.screens.mealplan.*

// Permissions Array (Keep as is)
private val REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES)
} else {
    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
}
private const val TAG = "MainActivityPermissions"

// SAMPLE FOR PROFILE
val sampleUser = UserProfile(
    id = "1",
    name = "John Smith",
    username = "johnsmith",
    bio = "I love cooking and I love planting 💚 follow me on yt too\n#Chef'sDaBest #JohnCooks",
    profilePictureUrl = "https://via.placeholder.com/100",
    backgroundImageUrl = "https://via.placeholder.com/600x200",
    links = listOf(
        UserLink("YouTube", "youtube.com/@johncooks")
    ),
    postsCount = 7,
    followersCount = 90030,
    followingCount = 100
)

val sampleRecipes = listOf(
    Recipe(
        id = 1,
        name = "Canned Tuna Pasta",
        imageRes = R.drawable.tryfood,
        tags = listOf(
            RecipeTag("Lunch"),
            RecipeTag("Italian")
        ),
        rating = 4.5
    ),
    Recipe(
        id = 2,
        name = "Vegetable Stir Fry",
        imageRes = R.drawable.tryfood,
        tags = listOf(
            RecipeTag("Dinner"),
            RecipeTag("Vegan")
        ),
        rating = 4.8
    ),
    Recipe(
        id = 3,
        name = "Vegetable Stir Fry",
        imageRes = R.drawable.tryfood,
        tags = listOf(
        RecipeTag("Dinner"),
        RecipeTag("Vegan")
    ),
        rating = 9.1
)
)



class MainActivity : ComponentActivity() {
    private var permissionsInitiallyGranted = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsInitiallyGranted = areAllPermissionsGranted(this, REQUIRED_PERMISSIONS)
        Log.d(TAG, "Permissions initially granted on create: $permissionsInitiallyGranted")
        setContent {
            HandlePermissions(
                permissions = REQUIRED_PERMISSIONS,
                onPermissionsGranted = { Log.d(TAG, "Compose notified: Permissions Granted") },
                onPermissionsDenied = { wasPermDenied ->
                    Log.w(TAG, "Compose notified: Permissions Denied. Perm: $wasPermDenied")
                }
            ) {
                MyApp() // Render MyApp regardless of initial grant, HandlePermissions shows dialogs
            }
        }
    }

    override fun onResume() {
        /* Keep onResume logic as is */
        super.onResume()
        /* ... */
    }
}

fun areAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
    return permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun HandlePermissions(
    permissions: Array<String>,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (permanentlyDenied: Boolean) -> Unit,
    content: @Composable (permissionsGranted: Boolean) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var permissionsGrantedState by remember { mutableStateOf(areAllPermissionsGranted(context, permissions)) }
    var showDeniedDialog by remember { mutableStateOf(false) }
    var showPermanentlyDeniedDialog by remember { mutableStateOf(false) }
    var didRequestPermissions by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val allGranted = permissionsMap.all { it.value }
        permissionsGrantedState = allGranted
        if (allGranted) {
            onPermissionsGranted()
        } else {
            val permanentlyDenied = permissionsMap.any { (perm, granted) ->
                !granted && activity?.shouldShowRequestPermissionRationale(perm) == false
            }
            if (permanentlyDenied) {
                showPermanentlyDeniedDialog = true
            } else {
                showDeniedDialog = true
            }
            onPermissionsDenied(permanentlyDenied)
        }
    }

    LaunchedEffect(permissions, permissionsGrantedState) {
        if (!permissionsGrantedState && !didRequestPermissions) {
            launcher.launch(permissions)
            didRequestPermissions = true
        }
    }

    content(permissionsGrantedState)

    if (showDeniedDialog) {
        PermissionAlertDialog(
            title = "Permissions Required",
            message = "Camera and Storage access needed for adding photos.",
            confirmButtonText = "Grant",
            onConfirm = {
                showDeniedDialog = false
                launcher.launch(permissions)
            },
            dismissButtonText = "Cancel",
            onDismiss = {
                showDeniedDialog = false
                Toast.makeText(context, "Feature limited.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showPermanentlyDeniedDialog) {
        PermissionAlertDialog(
            title = "Permissions Required",
            message = "Permissions permanently denied. Go to App Settings to grant them.",
            confirmButtonText = "Go to Settings",
            onConfirm = {
                showPermanentlyDeniedDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            },
            dismissButtonText = "Cancel",
            onDismiss = {
                showPermanentlyDeniedDialog = false
                Toast.makeText(context, "Image features disabled.", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun PermissionAlertDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    dismissButtonText: String,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { androidx.compose.material3.Text(text = title) },
        text = { androidx.compose.material3.Text(text = message) },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                androidx.compose.material3.Text(confirmButtonText)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                androidx.compose.material3.Text(dismissButtonText)
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O) // Make sure this annotation is required here
@Composable
fun MyApp() {
    val navController = rememberNavController()
    // Define startDestination (consider checking auth state later)
    // Use the full path as per your original structure
    val startDestination = ScreenNavigation.Screen.LogIn.route

    NavHost(navController, startDestination = startDestination) {
        // --- Authentication ---
        composable(ScreenNavigation.Screen.SignUp.route) { CreateAccountScreen(navController) }
        composable(ScreenNavigation.Screen.EmailVerification.route) { EmailVerificationScreen(navController) }
        composable(ScreenNavigation.Screen.AccountSuccessfullyCreated.route) { AccountSuccessfullyCreated(navController) }
        composable(ScreenNavigation.Screen.LogIn.route) { LoginScreen(navController) }
        composable(ScreenNavigation.Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                navController
            )
        }
        composable(ScreenNavigation.Screen.Verification.route) { VerificationScreen(navController) }
        composable(ScreenNavigation.Screen.NewPassword.route) { NewPasswordScreen(navController) }
        composable(ScreenNavigation.Screen.PasswordChanged.route) { PasswordChangeSuccessfullyScreen(navController) }

        // --- Main Features ---
        composable(ScreenNavigation.Screen.Home.route) { HomeScreen(navController) }
        composable(ScreenNavigation.Screen.Notification.route) { NotificationScreen(navController) }
        composable(ScreenNavigation.Screen.YourRecipes.route) { YourRecipeScreen(navController) } // Ensure this Composable exists
        composable(ScreenNavigation.Screen.AddRecipe.route) { NewRecipeScreen(navController) } // Ensure this Composable exists
        composable(ScreenNavigation.Screen.SearchRecipe.route) { InteractionSearchScreen(navController) } // Ensure this Composable exists
        // composable(ScreenNavigation.Screen.SearchResult.route) { /* Your Search Result Screen composable */ }

        // --- Collections ---
        composable(ScreenNavigation.Screen.NewCollection.route) {
            NewCollectionScreen( // Ensure this Composable exists
                onNavigateToNaming = { ids ->
                    // Pass the list directly to createRoute
                    navController.navigate(
                        ScreenNavigation.Screen.NamingCollection.createRoute(ids)
                    )
                },
                navController = navController
            )
        }
        composable(
            route = ScreenNavigation.Screen.NamingCollection.route,
            arguments = listOf(navArgument("recipeIds") { type = NavType.StringType })
        ) { backStackEntry ->
            val idsString = backStackEntry.arguments?.getString("recipeIds") ?: ""
            val idsList = idsString.split(",").filter { it.isNotEmpty() } // Parse back to list

            NamingCollectionScreen( // Ensure this Composable exists
                navController = navController,
                selectedRecipeIds = idsList
            )
        }

        // ★ Updated Nav Destination for CollectionDetail ★
        composable(
            route = ScreenNavigation.Screen.CollectionDetail.route, // Use route from Screen object: "collection_detail_screen/{collectionId}/{collectionName}"
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType /* nullable = false is default */ },
                navArgument("collectionName") { type = NavType.StringType } // Define argument for name
            )
        ) { backStackEntry ->
            // Extract arguments safely
            val collectionId = backStackEntry.arguments?.getString("collectionId")
            val encodedCollectionName =
                backStackEntry.arguments?.getString("collectionName") // Get potentially encoded name

            // Basic validation: Ensure required arguments are present
            if (collectionId != null && encodedCollectionName != null) {
                CollectionDetailScreen(
                    navController = navController,
                    collectionId = collectionId,
                    initialCollectionName = encodedCollectionName // Pass the potentially encoded name
                    // savedRecipesViewModel = yourViewModelInstance // Pass VM if needed
                )
            } else {
                // Handle error: Arguments missing (shouldn't happen if createRoute is used correctly)
                Log.e(
                    "NavHost",
                    "Error: Missing collectionId or collectionName for CollectionDetail route."
                )
                // Optionally navigate back or show an error UI
                navController.popBackStack()
            }
        }

        // ★ Added Nav Destination for CollectionEdit ★
        composable(
            route = ScreenNavigation.Screen.CollectionEdit.route, // Use route from Screen object
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType; nullable = false }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId")!!

            CollectionEditScreen( // Ensure this Composable exists
                navController = navController,
                collectionId = collectionId
            )
        }
        composable(ScreenNavigation.Screen.AddRecipe.route) { NewRecipeScreen(navController) }
        composable(ScreenNavigation.Screen.SearchRecipe.route) {
            InteractionSearchScreen(
                navController
            )
        }
        composable(ScreenNavigation.Screen.SearchResult.route) { navController }

        // --- Meal Plan ---
        composable(ScreenNavigation.Screen.MealPlan.route) {
            MealPlanScreen( // Ensure this Composable exists
                navController,
                onAddMealsToMealPlanClick = { type ->
                    navController.navigate(ScreenNavigation.Screen.AddMealsToMealPlan.createRoute(type))
                }
            )
        }
        composable(
            ScreenNavigation.Screen.AddMealsToMealPlan.route,
            arguments = listOf(navArgument("mealType") { type = NavType.StringType })
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("mealType") ?: "Unknown"
            AddMealsToMealPlanScreen(navController, type) // Ensure this Composable exists
        }
        composable(ScreenNavigation.Screen.AddMealPlan.route) {
            addMealPlanScreen( // Ensure this Composable exists (check capitalization: AddMealPlanScreen?)
                navController,
                onAddMealPlanClick = {
                    navController.navigate(ScreenNavigation.Screen.MealPlan.route) {
                        popUpTo(ScreenNavigation.Screen.MealPlan.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Recipe Detail & Edit ---
        //User Profile Screens
        composable(ScreenNavigation.Screen.UserProfile.route) {
            UserProfileScreen(
                userProfile = sampleUser/* your UserProfile object here */,
                navController = navController,
                // Optional: pass these if needed, otherwise defaults will apply
                onSearchClick = {
                    navController.navigate(ScreenNavigation.Screen.SearchRecipe.route)
                },
                onNotificationsClick = {  },
                onMenuClick = { /* handle menu */ },
                onLinkClick = { link -> /* handle link click */ },
                recipes = sampleRecipes,
                onRecipeClick = { recipe -> /* handle recipe click */ }
            )

        }
        composable(ScreenNavigation.Screen.EditProfile.route) {
            EditProfileScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onSaveClick = {}
            )
        }
        composable(ScreenNavigation.Screen.Privacy.route) {
            PrivacyScreen(
                onBackClick = { navController.popBackStack() },
                onAccountPrivacyClick = { navController.navigate(ScreenNavigation.Screen.AccountPrivacy.route) },
                onPrivacyPolicyClick = { navController.navigate(ScreenNavigation.Screen.PrivacyPolicy.route) },
                onTermsClick = { navController.navigate(ScreenNavigation.Screen.TermsAndConditions.route) },
                onFaqsClick = { navController.navigate(ScreenNavigation.Screen.Faqs.route) }
            )
        }
        composable(ScreenNavigation.Screen.AccountPrivacy.route) {
            AccountPrivacyScreen(
                onBackClick = { navController.popBackStack() },
                onSavePasswordClick = {}
            )
        }
        composable(ScreenNavigation.Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen (
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(ScreenNavigation.Screen.TermsAndConditions.route){
            TermsAndConditionsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(ScreenNavigation.Screen.Faqs.route) {
            FaqsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }


        // --- Recipe Detail ---
        composable(
            route = ScreenNavigation.Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.StringType
                nullable = false
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")!!
            RecipeDetailScreen( // Ensure this Composable exists
                navController = navController,
                recipeId = recipeId,
                onBackClicked = { navController.popBackStack() }
            )
        }
        composable(
            route = ScreenNavigation.Screen.EditRecipe.route,
            arguments = listOf(navArgument("recipeId") {
                type = NavType.StringType
                nullable = false
            })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")!!
            EditRecipeScreen( // Ensure this Composable exists
                navController = navController,
                recipeId = recipeId
            )
        }
    } // End NavHost
}