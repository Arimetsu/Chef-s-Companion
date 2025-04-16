package com.example.myapplication.components.recipegrids

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.front_end.userprofile.UserLink
import com.example.myapplication.front_end.userprofile.UserProfile
import com.example.myapplication.front_end.userprofile.UserProfileScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

//import com.example.myapplication.front_end.userprofile.RecipeTag

//To be remove pato pag may data models na
data class RecipeTag(val name: String)

data class Recipe(
    val id: Int,
    val name: String,
    val imageRes: Int,
    val tags: List<RecipeTag>,
    val rating: Double
)

val recipes = listOf(
    Recipe(
        id = 1,
        name = "Canned Tuna Pasta",
        imageRes = R.drawable.tryfood,
        tags = listOf(
            RecipeTag("Lunch"),
            RecipeTag("Italian")
        ),
        rating = 9.5
    ),
    // Add more recipes as needed
)



@Composable
fun RecipeCard(
    recipe: Recipe,
    onRecipeClick: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onRecipeClick(recipe) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Recipe Image
            Image(
                painter = painterResource(id = R.drawable.tryfood),
                contentDescription = recipe.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ){
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    // Display Rating
                    RatingTag(rating = recipe.rating)

                    // Display Tags
                    recipe.tags.forEach { tag ->
                        RecipeTag(tag = tag)
                    }
                }
            }
            // Bottom overlay and title
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,  // Top = Transparent (fades into the image)
                                MaterialTheme.colorScheme.primary  // Bottom = Solid color
                            ),
                            startY = 0f,  // Top of the column
                            endY = Float.POSITIVE_INFINITY  // Extends to bottom
                        )
                    )
                    .padding(16.dp)
            ) {
                // Recipe tags

                // Recipe name
                Text(
                    text = recipe.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RatingTag(rating: Double){
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(255, 255, 255, 200),
        contentColor = Color.Black
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Icon (
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Rating",
                tint = Color(255,185,0),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = rating.toString(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun RecipeTag(tag: RecipeTag) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(255, 255, 255, 200),
        contentColor = Color.Black
    ) {
        Text(
            text = tag.name,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun UserProfileScreenPreview() {
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
        postsCount = 3,
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
            name = "Sample",
            imageRes = R.drawable.tryfood,
            tags = listOf(
                RecipeTag("Breakfast"),
                RecipeTag("Filipino")
            ),
            rating = 9.1
        )
    )



    MyApplicationTheme {
        UserProfileScreen(
            navController = NavController(LocalContext.current),
            userProfile = sampleUser,
            recipes = sampleRecipes,
            onRecipeClick = { recipe ->
                // Handle recipe click
            }
        )
    }
}
