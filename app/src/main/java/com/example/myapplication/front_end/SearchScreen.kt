import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.front_end.monte

data class RecentSearchItem(
    val type: SearchItemType,
    val label: String,
    val secondaryLabel: String? = null,
    val imageResId: Int? = null // Optional image resource ID
)

enum class SearchItemType {
    USER, QUERY, RECIPE
}

@Composable
fun InteractionSearchScreen(navController: NavHostController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            SearchBar(navController)
            RecentSearches(
                recentItems = listOf(
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian",  R.drawable.tryfood),
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian",  R.drawable.tryfood),
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian",  R.drawable.tryfood),
                    RecentSearchItem(SearchItemType.USER, "xampleeuser", "sample name\n20.2M followers", R.drawable.user),
                    RecentSearchItem(SearchItemType.QUERY, "meowmeow"),
                    RecentSearchItem(SearchItemType.RECIPE, "Canned Tuna Pasta", "xampleeuser\nitalian", R.drawable.tryfood),
                )
            )
        }
    }


@Composable
fun SearchBar(navController: NavHostController) {
    var search by remember { mutableStateOf("") }
    Spacer(modifier = Modifier.height(24.dp))
    Column(
        horizontalAlignment = Alignment.End,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = { navController.navigate("home") }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it /*TODO: Handle text input*/ },
                placeholder = { Text("Search for a recipe") },
                modifier = Modifier
                    .weight(1f),
                shape = MaterialTheme.shapes.medium
            )
            Spacer(modifier = Modifier.width(8.dp))

        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { /*TODO: Handle filter*/ },
            modifier = Modifier
                .width(75.dp)
                .height(25.dp),
            shape = RoundedCornerShape(size = 12.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFFE0E0E0), // Light gray background
                contentColor = Color.Black // Text and icon color
            ),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), // Adjust padding for smaller size
            elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp) // Remove elevation/border
        ) {
            Icon(
                painter = painterResource(R.drawable.filter), // Replace with your filter icon resource
                contentDescription = "Filter",
                tint = Color.Black,
                modifier = Modifier.size(16.dp) // Adjust icon size for smaller button
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Filter", fontSize = 10.sp,
                fontFamily = monte,

                )
        }
    }
}

@Composable
fun RecentSearches(recentItems: List<RecentSearchItem>) {
    Text(
        text = "Recent",
        fontSize = 16.sp,
        fontFamily = monte,
        fontWeight = FontWeight(600)
    )
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        items(recentItems) { item ->
            RecentSearchItemRow(item = item)
            Divider()
        }
    }
}

@Composable
fun RecentSearchItemRow(item: RecentSearchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (item.type) {
            SearchItemType.USER -> {
                Image(
                    painter = painterResource(R.drawable.user), // Use placeholder if no image
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(item.label,
                        fontWeight = FontWeight(500),
                        fontFamily = monte,
                        fontSize = 12.sp,
                        )
                    item.secondaryLabel?.let {
                        Text(it, fontSize = 12.sp, color = Color.Gray,
                            fontWeight = FontWeight(500))
                    }
                }
            }
            SearchItemType.QUERY -> {
                Icon(Icons.Filled.Search, contentDescription = "Search Query", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text(item.label,
                    fontFamily = monte,
                    fontWeight = FontWeight(500))
            }
            SearchItemType.RECIPE -> {
                Image(
                    painter = painterResource(R.drawable.tryfood), // Use placeholder if no image
                    contentDescription = "User Avatar",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RectangleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text(item.label, fontWeight = FontWeight(500),
                        fontFamily = monte, )
                    item.secondaryLabel?.let {
                        Text(it, fontSize = 12.sp,
                            fontFamily = monte,
                            color = Color.Gray,
                            fontWeight = FontWeight(500))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /*TODO: Handle item removal*/ }) {
            Icon(Icons.Filled.Close, contentDescription = "Remove")
        }
    }
}