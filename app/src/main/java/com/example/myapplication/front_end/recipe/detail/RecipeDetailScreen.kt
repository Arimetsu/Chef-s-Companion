package com.example.myapplication.front_end.recipe.detail

import android.R.attr.text
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape // Import CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // Import AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.IngredientItem
import com.example.myapplication.data.NutritionItem
import com.example.myapplication.data.RecipeDetail
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.common_composable.RecipeTag
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.front_end.recipe.find.DarkGreen
import com.example.myapplication.front_end.recipe.find.LightGreenBackground
import com.example.myapplication.viewModel.RecipeDetailState
import com.example.myapplication.viewModel.RecipeDetailViewModel
import kotlinx.coroutines.delay

val MutedGray = Color.Gray
val SectionBackgroundColor = LightGreenBackground
val FavoriteColor = Color.Red
val RatingStarColor = Color(0xFFFFC107) // Gold/Yellow

// Constants
private const val RATING_SAVE_DELAY_MS = 3000L // 3 seconds delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeId: String, // Pass recipeId instead of the whole object initially
    recipeDetailViewModel: RecipeDetailViewModel = viewModel(), // Inject ViewModel
    onBackClicked: () -> Unit
    // Remove callbacks for rating save, favorite, bookmark
) {
    // Fetch data when the screen is composed or recipeId changes
    LaunchedEffect(recipeId) {
        recipeDetailViewModel.fetchRecipeById(recipeId)
    }

    // Observe the state from the ViewModel
    val state by recipeDetailViewModel.recipeDetailState.collectAsStateWithLifecycle()

    // Handle different states
    when (val currentState = state) {
        is RecipeDetailState.Loading -> {
            // Show a loading indicator
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreen)
            }
        }
        is RecipeDetailState.Success -> {
            // Show the recipe details using the data from the state
            RecipeDetailContent(
                navController = navController,
                recipeDetail = currentState.recipeDetail,
                isOwner = currentState.isOwner,
                viewModel = recipeDetailViewModel, // Pass ViewModel for actions
                onBackClicked = onBackClicked
            )
        }
        is RecipeDetailState.Error -> {
            // Show error message
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error loading recipe: ${currentState.message}\nTap to retry.",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable { recipeDetailViewModel.fetchRecipeById(recipeId) } // Allow retry
                )
            }
        }
        is RecipeDetailState.Idle -> {
            // Show loading or placeholder, Idle shouldn't usually be shown long
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DarkGreen) // Show loading initially
            }
        }
    }
}


// --- Main Content Composable (Extracted from previous Scaffold body) ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RecipeDetailContent(
    navController: NavController,
    recipeDetail: RecipeDetail,
    isOwner: Boolean,
    viewModel: RecipeDetailViewModel, // Receive ViewModel
    onBackClicked: () -> Unit
) {
    // Local state for the rating input (reflects user interaction *before* saving)
    // Initialize with the rating from the ViewModel state
    var userRatingInput by rememberSaveable(recipeDetail.id, recipeDetail.userRating) {
        mutableStateOf(recipeDetail.userRating)
    }

    // Effect to automatically save the rating after a delay
    LaunchedEffect(userRatingInput, recipeDetail.id) {
        // Only trigger save if the input rating is different from the confirmed rating
        if (userRatingInput != recipeDetail.userRating) {
            delay(RATING_SAVE_DELAY_MS)
            // After delay, check *again* if it's still different (user might have changed it back)
            if (userRatingInput != recipeDetail.userRating) {
                Log.d("RecipeDetailScreen", "Debounced save triggered for rating: $userRatingInput")
                viewModel.updateRecipeRating(recipeDetail.id, userRatingInput)
            } else {
                Log.d("RecipeDetailScreen", "Debounced save cancelled, rating reverted to ${recipeDetail.userRating}")
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box( modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center ) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally ) {
                            Text( text = recipeDetail.title, style = TextStyle( fontSize = 24.sp, fontFamily = monte, fontWeight = FontWeight.Bold, color = DarkGreen, textAlign = TextAlign.Center ), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            // Display Author Name
                            Text( "By ${recipeDetail.authorName}", style = MaterialTheme.typography.bodySmall, color = MutedGray )
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBackClicked) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGreen) } },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = {
                            // Navigate to Edit Screen
                            navController.navigate(ScreenNavigation.Screen.EditRecipe.createRoute(recipeDetail.id))
                        }) { Icon(Icons.Filled.Edit, contentDescription = "Edit Recipe", tint = DarkGreen) }
                    } else {
                        // Placeholder to maintain layout balance if needed, or remove Spacer
                        Spacer(Modifier.width(48.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
            )
        },
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(color = Color.White).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // --- Image Header ---
            item {
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(
                        model = recipeDetail.imageUrl,
                        contentDescription = recipeDetail.title,
                        placeholder = painterResource(R.drawable.greenbackgroundlogo),
                        error = painterResource(R.drawable.greenbackgroundlogo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Row(modifier=Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        // Favorite Toggle - Calls ViewModel directly
                        IconToggleButton(
                            checked = recipeDetail.isFavorite,
                            onCheckedChange = {
                                // No need for isChecked parameter, toggle based on current state
                                viewModel.toggleFavoriteStatus(recipeDetail.id, recipeDetail.isFavorite)
                            },
                            modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha=0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (recipeDetail.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (recipeDetail.isFavorite) FavoriteColor else Color.White
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        // Bookmark Toggle - Calls ViewModel directly
                        IconToggleButton(
                            checked = recipeDetail.isBookmarked,
                            onCheckedChange = {
                                viewModel.toggleBookmarkStatus(recipeDetail.id, recipeDetail.isBookmarked)
                            },
                            modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha=0.3f), CircleShape)
                        ) {
                            // Consider using an Icon for consistency if you have a bookmark icon vector
                            Image(
                                painter = painterResource(
                                    id = if (recipeDetail.isBookmarked) R.drawable.bookmark_filled else R.drawable.bookmark // Ensure these drawables exist
                                ),
                                contentDescription = "Bookmark",
                                // You might want to tint the bookmark icon as well
                                // colorFilter = ColorFilter.tint(if (recipeDetail.isBookmarked) SomeBookmarkColor else Color.White)
                            )
                        }
                    }
                }
            }

            // --- Recipe Header Info (Tags) ---
            item {
                RecipeDetailHeaderInfo(
                    tags = recipeDetail.tags,
                    // ★ ADDED average rating and count display
                    averageRating = recipeDetail.averageRating,
                    ratingCount = recipeDetail.ratingCount
                )
            }


            // --- User Rating Section ---
            item {
                Column(modifier = Modifier.padding(top = 0.dp)) { // Reduced top padding
                    Text(
                        text = "Your Rating:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    // Rating input uses the local 'userRatingInput' state
                    RatingInputRow(
                        currentRating = userRatingInput,
                        onRatingChange = { newRating ->
                            // Update the local input state immediately for responsiveness
                            userRatingInput = newRating
                            // The LaunchedEffect will handle the delayed save
                        }
                    )
                    // Optional: Display confirmation or loading state while saving
                    // if (viewModel.recipeUpdateState.collectAsState().value == RecipeUpdateState.Loading) ...
                }
            }

            // --- Other Sections ---
            item { RecipeTimeServingInfo( servingSize = recipeDetail.servingSize, cookingTime = recipeDetail.cookingTime, prepTime = recipeDetail.prepTime ) }
            item { RecipeSection(title = "Ingredients") { IngredientList(ingredients = recipeDetail.ingredients) } }
            item {
                RecipeSection(title = "Procedure") {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        recipeDetail.procedureSteps.forEachIndexed { index, step ->
                            Text( text = "${index + 1}. $step", style = MaterialTheme.typography.bodyLarge )
                        }
                    }
                }
            }
            if (recipeDetail.personalNote != null && recipeDetail.personalNote.isNotBlank()) {
                item { RecipeSection(title = "Personal Note") { Text( text = recipeDetail.personalNote, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp) ) } }
            }
            if (!recipeDetail.nutritionFacts.isNullOrEmpty()) {
                item { RecipeSection(title = "Nutrition Facts") { NutritionTable(facts = recipeDetail.nutritionFacts) } }
            }

            // --- REMOVED Save Rating Button ---
            // item { ... Button ... }

            item { Spacer(Modifier.height(16.dp)) } // Add some padding at the bottom
        }
    }
}

// --- Updated Header Info Composable ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailHeaderInfo(
    tags: List<String>,
    averageRating: Double, // ★ ADDED
    ratingCount: Int      // ★ ADDED
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Display Average Rating and Count
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp) // Add padding if needed
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = "Average Rating",
                tint = RatingStarColor,
                modifier = Modifier.size(18.dp) // Slightly smaller star
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(String.format("%.1f", averageRating)) // Format to 1 decimal place
                    }
                    append(" ") // Add space
                    withStyle(style = SpanStyle(color = MutedGray, fontSize = 12.sp)) {
                        append("($ratingCount rating${if (ratingCount != 1) "s" else ""})")
                    }
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Display Tags using FlowRow
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tags.forEach { tag ->
                    RecipeTag(text = tag)
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp)) // Maintain space if no tags
        }
    }
}


// --- Helper Composables (RatingInputRow, RecipeTimeServingInfo, etc.) remain largely the same ---
// Make sure RatingInputRow uses appropriate Star icons (Filled.Star, Outlined.Star or StarOutline)

@Composable
fun RatingInputRow(
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    currentRating: Int, // This is the INPUT rating state
    onRatingChange: (Int) -> Unit,
    starColor: Color = RatingStarColor,
    emptyStarColor: Color = MutedGray.copy(alpha = 0.5f) // Make empty stars less prominent
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (starIndex in 1..maxRating) {
            val isSelected = starIndex <= currentRating
            IconButton(
                onClick = {
                    onRatingChange(starIndex)
                },
                modifier = Modifier.size(36.dp).padding(0.dp)
            ) {
                Image(
                    painter = painterResource(id = if (isSelected) R.drawable.star else R.drawable.empty_star),
                    contentDescription = "Rate $starIndex",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        // Optional: Add a clear button if needed
        // IconButton(onClick = { onRatingChange(0) }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Clear, ...)}
    }
}

// --- Add these Helper Composables to RecipeDetailScreen.kt ---

@Composable
fun RecipeTimeServingInfo(
    servingSize: Int, // Changed from Long back to Int as per RecipeDetail
    cookingTime: String,
    prepTime: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround, // Distribute space evenly
        verticalAlignment = Alignment.Top // Align items to the top
    ) {
        // Use weight to distribute space more reliably if needed, or SpaceAround is often fine
        InfoColumn(label = "Serving", value = servingSize.toString(), unit = "(Person)")
        InfoColumn(
            label = "Prep Time", // Swapped order to match common convention
            value = prepTime.takeIf { it.isNotEmpty() } ?: "--",
            iconPainter = painterResource(R.drawable.restaurant) // Use appropriate icons
        )
        InfoColumn(
            label = "Cook Time",
            value = cookingTime.takeIf { it.isNotEmpty() } ?: "--",
            iconPainter = painterResource(R.drawable.alarm) // Use appropriate icons
        )
    }
}

@Composable
fun InfoColumn(
    label: String,
    value: String,
    unit: String? = null,
    iconPainter: Painter? = null // Use Painter for icons
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter, // Use painter directly
                    contentDescription = null, // Icon is decorative
                    tint = MutedGray,
                    modifier = Modifier.size(14.dp) // Consistent icon size
                )
                Spacer(Modifier.width(4.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium, // Use MaterialTheme styles
                color = MutedGray
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium // Slightly bolder value
        )
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall, // Smaller unit text
                color = MutedGray
            )
        }
    }
}

@Composable
fun RecipeSection(
    title: String,
    hint: String? = null, // Optional hint next to title
    content: @Composable ColumnScope.() -> Unit // Content lambda
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium, // Section title style
                fontWeight = FontWeight.SemiBold
            )
            if (hint != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedGray
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        // Use Surface for background color and shape
        Surface(
            shape = RoundedCornerShape(12.dp), // Consistent rounding
            color = SectionBackgroundColor, // Use defined background color
            modifier = Modifier.fillMaxWidth() // Take full width
        ) {
            // Column to hold the actual content passed in the lambda
            Column(content = content)
        }
    }
}

@Composable
fun IngredientList(ingredients: List<IngredientItem>) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), // Padding inside the section
        verticalArrangement = Arrangement.spacedBy(6.dp) // Space between ingredient items
    ) {
        ingredients.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Align text nicely
            ) {
                Text(
                    // Combine quantity and unit, handle null/empty unit
                    text = "${item.quantity} ${item.unit?.takeIf { it.isNotEmpty() } ?: ""}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(80.dp) // Fixed width for quantity/unit column
                )
                Spacer(Modifier.width(8.dp)) // Space between columns
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f) // Ingredient name takes remaining space
                )
            }
        }
    }
}

@Composable
fun NutritionTable(facts: List<NutritionItem>) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        // Optional Header Row
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(
                "Nutrition Per Serving (Approx.)", // Or just "Nutrition Facts"
                style = MaterialTheme.typography.titleSmall, // Style for header
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f) // Takes most space
            )
            // You could add headers for "Amount" and "% DV" if desired
        }
        Divider(color = MutedGray.copy(alpha = 0.5f), thickness = 1.dp) // Thicker divider below header
        facts.forEach { fact ->
            NutritionRow(fact = fact)
            // Add a lighter divider between rows
            Divider(color = MutedGray.copy(alpha = 0.2f))
        }
    }
}

@Composable
fun NutritionRow(fact: NutritionItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), // Vertical padding for each row
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nutrient Name
        Text(
            text = fact.nutrient,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f) // Nutrient name takes most space
        )
        // Amount (if available)
        if (fact.amount != null) {
            Text(
                text = fact.amount,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End, // Align amount to the right
                // Minimum width for amount column, padding for spacing
                modifier = Modifier.widthIn(min = 50.dp).padding(horizontal = 8.dp)
            )
        }
        // Daily Value (if available)
        if (fact.dailyValue != null) {
            Text(
                text = fact.dailyValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold, // Daily Value often bolded
                textAlign = TextAlign.End, // Align %DV to the right
                modifier = Modifier.width(40.dp) // Fixed width for %DV column
            )
        } else if (fact.amount != null) {
            // Add a spacer if only amount is present, to maintain alignment
            Spacer(Modifier.width(40.dp))
        }
    }
}

