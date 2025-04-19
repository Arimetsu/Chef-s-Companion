package com.example.myapplication.front_end.recipe.detail

import android.R.attr.text
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // Import AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.IngredientItem
import com.example.myapplication.data.NutritionItem
import com.example.myapplication.data.RecipeDetail
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.front_end.home.monte
import com.example.myapplication.front_end.recipe.find.DarkGreen
import com.example.myapplication.front_end.recipe.find.LightGreenBackground

val MutedGray = Color.Gray
val SectionBackgroundColor = LightGreenBackground
val FavoriteColor = Color.Red
val RatingStarColor = Color(0xFFFFC107)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecipeDetailScreen(
    navController: NavController,
    recipeDetail: RecipeDetail,
    isOwner: Boolean,
    onBackClicked: () -> Unit,
    onRatingSaveClicked: (Int) -> Unit, // Pass only the new rating
    onFavoriteToggleClicked: (Boolean) -> Unit, // Pass new desired state (true=add, false=remove)
    onBookmarkToggleClicked: (Boolean) -> Unit // Pass new desired state
) {
    // Local state only needed for the rating input before saving
    var userRating by rememberSaveable(recipeDetail.id) { mutableStateOf(recipeDetail.userRating) }

    // isFavorite and isBookmarked are now directly read from recipeDetail for display

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box( modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center ) {
                        Column( horizontalAlignment = Alignment.CenterHorizontally ) {
                            Text( text = recipeDetail.title, style = TextStyle( fontSize = 24.sp, fontFamily = monte, fontWeight = FontWeight.Bold, color = DarkGreen, textAlign = TextAlign.Center ), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Text( "By ${recipeDetail.authorName}", style = MaterialTheme.typography.bodySmall, color = MutedGray )
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBackClicked) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = DarkGreen) } },
                actions = {
                    if (isOwner) {
                        IconButton(onClick = {
                            // TODO: Navigate to Edit Screen
                            // navController.navigate(ScreenNavigation.Screen.EditRecipe.createRoute(recipeDetail.id))
                        }) { Icon(Icons.Filled.Edit, contentDescription = "Edit Recipe", tint = DarkGreen) }
                    } else { Spacer(Modifier.width(48.dp)) }
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
                    // Add scrim if needed for overlay buttons
                    // Box(modifier=Modifier.fillMaxSize().background(Brush.verticalGradient(...)))
                    Row(modifier=Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        // Favorite Toggle
                        IconToggleButton(
                            checked = recipeDetail.isFavorite, // Use state from recipeDetail
                            onCheckedChange = { onFavoriteToggleClicked(!recipeDetail.isFavorite) }, // Pass the desired NEW state
                            modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha=0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (recipeDetail.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (recipeDetail.isFavorite) FavoriteColor else Color.White
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        // Bookmark Toggle
                        IconToggleButton(
                            checked = recipeDetail.isBookmarked, // Use state from recipeDetail
                            onCheckedChange = { onBookmarkToggleClicked(!recipeDetail.isBookmarked) },
                            modifier = Modifier.size(40.dp).background(Color.Black.copy(alpha=0.3f), CircleShape)
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (recipeDetail.isBookmarked) R.drawable.bookmark_filled else R.drawable.bookmark
                                ),
                                contentDescription = "Bookmark"
                            )
                        }
                    }
                }
            }


            item {
                RecipeDetailHeaderInfo( // Separated Header Info from Toggles
                    title = recipeDetail.title,
                    tags = recipeDetail.tags
                )
            }

            item {
                Column(modifier = Modifier.padding(top = 0.dp)) { // Reduced top padding
                    Text( text = "Your Rating:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 4.dp) )
                    RatingInputRow( currentRating = userRating, onRatingChange = { newRating -> userRating = newRating } )
                }
            }

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

            item { // Save Rating Button
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onRatingSaveClicked(userRating) }, // Pass only the rating value
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                ) {
                    Text("Save Rating", fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// New Header Info Composable (Title and Tags)
@OptIn(ExperimentalLayoutApi::class) // Opt-in for Experimental FlowRow
@Composable
fun RecipeDetailHeaderInfo(
    title: String, // Title is now in TopAppBar, remove if not needed
    tags: List<String>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // If you still want the title here for some reason, uncomment:
        /*
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall, // Or appropriate style
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp) // Add padding if title is present
        )
        */
        if (tags.isNotEmpty()) { // Only show FlowRow if there are tags
            FlowRow(
                modifier = Modifier.fillMaxWidth(), // Allow FlowRow to take full width
                horizontalArrangement = Arrangement.spacedBy(8.dp), // Horizontal space between items
                verticalArrangement = Arrangement.spacedBy(4.dp) // Vertical space if items wrap
            ) {
                tags.forEach { tag ->
                    RecipeTag(text = tag) // Display each tag using the helper composable
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp)) // Maintain some minimal space
        }
    }
}


// --- Helper Composables (RatingInputRow, RecipeTimeServingInfo, etc.) ---
@Composable
fun RatingInputRow(
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    currentRating: Int,
    onRatingChange: (Int) -> Unit,
    starColor: Color = RatingStarColor,
    emptyStarColor: Color = MutedGray
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (starIndex in 1..maxRating) {
            val isSelected = starIndex <= currentRating
            IconButton(
                onClick = { onRatingChange(starIndex) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rate $starIndex",
                    tint = if (isSelected) starColor else emptyStarColor,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun RecipeTimeServingInfo(
    servingSize: Int,
    cookingTime: String,
    prepTime: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Top
    ) {
        InfoColumn(label = "Serving", value = servingSize.toString(), unit = "(Person)")
        InfoColumn(
            label = "Cooking Time",
            value = cookingTime.takeIf { it.isNotEmpty() } ?: "--",
            iconPainter = painterResource(R.drawable.alarm)
        )
        InfoColumn(
            label = "Preparation Time",
            value = prepTime.takeIf { it.isNotEmpty() } ?: "--",
            iconPainter = painterResource(R.drawable.restaurant)
        )
    }
}

@Composable
fun InfoColumn(
    label: String,
    value: String,
    unit: String? = null,
    iconPainter: Painter? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MutedGray
            )
            if (iconPainter != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    iconPainter,
                    contentDescription = null,
                    tint = MutedGray,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = MutedGray
            )
        }
    }
}

@Composable
fun RecipeSection(
    title: String,
    hint: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SectionBackgroundColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun IngredientList(ingredients: List<IngredientItem>) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ingredients.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.quantity} ${item.unit ?: ""}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(80.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NutritionTable(facts: List<NutritionItem>) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            Text(
                "Nutrition Per Serving",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f))
        }
        Divider(color = MutedGray.copy(alpha = 0.5f))
        facts.forEach { fact ->
            NutritionRow(fact = fact)
            Divider(color = MutedGray.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun NutritionRow(fact: NutritionItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = fact.nutrient,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f))
        if (fact.amount != null) {
            Text(
                text = fact.amount,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
                modifier = Modifier.widthIn(min = 50.dp).padding(horizontal = 8.dp))
        }
        if (fact.dailyValue != null) {
            Text(
                text = fact.dailyValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.width(40.dp))
        } else if (fact.amount != null) {
            Spacer(Modifier.width(40.dp))
        }
    }
}

@Composable
fun RecipeTag(text: String) {
    val tagShape = RoundedCornerShape(50)
    val tagBorderColor = Color(0xFFBDBDBD)
    val tagBackgroundColor = Color.White
    Box(
        modifier = Modifier
            .clip(tagShape)
            .background(tagBackgroundColor)
            .border(BorderStroke(1.dp, tagBorderColor), tagShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = LocalContentColor.current)
    }
}
// Previews remain the same
// ...