package com.example.myapplication.front_end.search

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.data.SearchResultItem
import com.example.myapplication.front_end.ScreenNavigation
import com.example.myapplication.viewModel.SearchViewModel
import com.example.myapplication.viewModel.SearchState
import com.example.myapplication.utils.RecentSearchManager


// Define data classes locally or import if defined elsewhere
// Ensure these match the definitions used in your ViewModel and RecentSearchManager
enum class SearchItemType { USER, QUERY, RECIPE }
data class RecentSearchItem(
    val type: SearchItemType,
    val label: String,
    val secondaryLabel: String? = null,
    val imageUrl: String? = null
)


// Color definition
val PrimaryGreen = Color(0xFF1A4D2E)
val RatingStarColor = Color(0xFFFFC107)

@Composable
fun InteractionSearchScreen(
    navController: NavHostController,
    searchViewModel: SearchViewModel = viewModel()
) {
    val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by searchViewModel.searchState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    // Need instance for RecentSearchManager if showing recents on EmptyQuery state directly
    val context = LocalContext.current
    val recentSearchManager = remember { RecentSearchManager(context) } // Only if needed for EmptyQuery

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchViewModel.updateSearchQuery(it) },
                    onClear = { searchViewModel.updateSearchQuery("") },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            when (val state = searchState) {
                is SearchState.Recent -> {
                    RecentSearches(
                        recentItems = state.items,
                        onItemClick = { item ->
                            focusManager.clearFocus()
                            searchViewModel.updateSearchQuery(item.label)
                        },
                        onRemoveClick = { item -> searchViewModel.removeRecentSearch(item) },
                        onClearAll = { searchViewModel.clearRecentSearches() }
                    )
                }
                is SearchState.EmptyQuery -> {
                    // Fetch and show recents when query is empty
                    val recentItems = remember(searchState) { // Re-fetch only when state becomes EmptyQuery
                        recentSearchManager.getRecentSearches()
                    }
                    RecentSearches(
                        recentItems = recentItems,
                        onItemClick = { item ->
                            focusManager.clearFocus()
                            searchViewModel.updateSearchQuery(item.label) },
                        onRemoveClick = { item -> searchViewModel.removeRecentSearch(item) },
                        onClearAll = { searchViewModel.clearRecentSearches() }
                    )
                }
                is SearchState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                }
                is SearchState.Success -> {
                    if (state.results.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            Text("No results found for '$searchQuery'.", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        SearchResultList(
                            results = state.results,
                            onRecipeClick = { recipeResult ->
                                focusManager.clearFocus()
                                searchViewModel.addItemToRecent(recipeResult)
                                navController.navigate(ScreenNavigation.Screen.RecipeDetail.createRoute(recipeResult.id))
                            },
                            onUserClick = { userResult ->
                                focusManager.clearFocus()
                                searchViewModel.addItemToRecent(userResult)
                                // TODO: Navigate to Profile Screen
                                Log.d("Search", "Clicked user: ${userResult.username}")
                            }
                        )
                    }
                }
                is SearchState.Error -> {
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Error searching: ${state.message}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryGreen)
        }
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search recipes or users", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            shape = CircleShape,
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear Search", tint = Color.Gray)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                cursorColor = PrimaryGreen,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun RecentSearches(
    recentItems: List<RecentSearchItem>,
    onItemClick: (RecentSearchItem) -> Unit,
    onRemoveClick: (RecentSearchItem) -> Unit,
    onClearAll: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text( text = "Recent Searches", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold )
            if (recentItems.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text("Clear All", style = MaterialTheme.typography.bodySmall, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                }
            }
        }

        if (recentItems.isEmpty()) {
            Text("No recent searches.", color = Color.Gray, modifier = Modifier.padding(vertical = 16.dp))
        } else {
            LazyColumn {
                items(recentItems, key = { "${it.type}-${it.label}" }) { item ->
                    RecentSearchItemRow(
                        item = item,
                        onClick = { onItemClick(item) },
                        onRemove = { onRemoveClick(item) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                }
            }
        }
    }
}

@Composable
fun RecentSearchItemRow(
    item: RecentSearchItem,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconTint = MaterialTheme.colorScheme.onSurfaceVariant
        val imageModifier = Modifier.size(40.dp)

        when (item.type) {
            SearchItemType.USER -> {
                AsyncImage(
                    model = item.imageUrl ?: R.drawable.user,
                    contentDescription = "User Avatar",
                    placeholder = painterResource(R.drawable.user),
                    error = painterResource(R.drawable.user),
                    modifier = imageModifier.clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
            SearchItemType.QUERY -> {
                Icon(
                    painter = painterResource(id = R.drawable.history),
                    contentDescription = "Search Query",
                    tint = iconTint,
                    modifier = imageModifier.padding(8.dp)
                )
            }
            SearchItemType.RECIPE -> {
                AsyncImage(
                    model = item.imageUrl ?: R.drawable.greenbackgroundlogo,
                    contentDescription = "Recipe Image",
                    placeholder = painterResource(R.drawable.greenbackgroundlogo),
                    error = painterResource(R.drawable.greenbackgroundlogo),
                    modifier = imageModifier.clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                item.label,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.secondaryLabel?.let {
                Text(
                    it.replace("\n", " • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove",
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SearchResultList(
    results: List<SearchResultItem>,
    onRecipeClick: (SearchResultItem.RecipeResult) -> Unit,
    onUserClick: (SearchResultItem.UserResult) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            "Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            items(
                results,
                key = { item ->
                    when(item) {
                        is SearchResultItem.RecipeResult -> "recipe-${item.id}"
                        is SearchResultItem.UserResult -> "user-${item.uid}"
                    }
                }
            ) { item ->
                when (item) {
                    is SearchResultItem.RecipeResult ->
                        RecipeResultCard(
                            recipeResult = item,
                            onClick = { onRecipeClick(item) }
                        )
                    is SearchResultItem.UserResult ->
                        UserResultRow(
                            userResult = item,
                            onClick = { onUserClick(item) }
                        )
                }
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
fun RecipeResultCard(
    recipeResult: SearchResultItem.RecipeResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = recipeResult.imageUrl ?: R.drawable.greenbackgroundlogo,
            contentDescription = recipeResult.name,
            placeholder = painterResource(R.drawable.greenbackgroundlogo),
            error = painterResource(R.drawable.greenbackgroundlogo),
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                recipeResult.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "Rating",
                    tint = RatingStarColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    String.format("%.1f", recipeResult.averageRating),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    " by ${recipeResult.authorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun UserResultRow(
    userResult: SearchResultItem.UserResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = userResult.profileImageUrl ?: R.drawable.user,
            contentDescription = userResult.username,
            placeholder = painterResource(R.drawable.user),
            error = painterResource(R.drawable.user),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                userResult.username,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
