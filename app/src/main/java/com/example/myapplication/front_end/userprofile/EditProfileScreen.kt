package com.example.myapplication.front_end.userprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.colorspace.WhitePoint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    var displayName by remember { mutableStateOf("John Smith") }
    var username by remember { mutableStateOf("@johnsmith") }
    var bio by remember { mutableStateOf("I love cooking and I love planting 🌱 follow me on yt too\n#ChefsDaBest #JohnCooks") }
    var link by remember { mutableStateOf("youtube.com/@johncooks") }

    MyApplicationTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface // Match the body background
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Edit Profile",
                        style = Typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))

                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E5631))
                        .clickable { /* Handle click */ },
                    contentAlignment = Alignment.Center
                ) {
                    // If you have a profile picture, use Image instead
                    // Image(
                    //    painter = painterResource(id = R.drawable.profile_picture),
                    //    contentDescription = "Profile Picture",
                    //    modifier = Modifier.fillMaxSize(),
                    //    contentScale = ContentScale.Crop
                    // )
                }

                TextButton(onClick = { /* Handle edit picture click */ }) {
                    Text(
                        text = "Edit picture or avatar",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display Name Field
                ProfileField(
                    label = "Display Name",
                    value = displayName,
                    onValueChange = { displayName = it }
                )

                // Username Field
                ProfileField(
                    label = "Username",
                    value = username,
                    onValueChange = { username = it }
                )

                // Bio Field
                ProfileField(
                    label = "Bio",
                    value = bio,
                    onValueChange = { bio = it },
                    singleLine = false,
                    maxLines = 5
                )

                // Links Section
                Text(
                    text = "Links",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    style = TextStyle(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                )

                // Add External Links Button
                OutlinedButton(
                    onClick = { /* Handle add links */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add external links")
                }

                // Existing Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_link),
                        contentDescription = "Link",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.size(24.dp) // Optional: match icon size
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = link,
                        style = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Save Button
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .padding(vertical = 24.dp)
                        .fillMaxWidth(0.5f)
                ) {
                    Text("Save profile")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
            )
        )

        Divider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.Gray.copy(alpha = 0.3f)
        )
    }
}
