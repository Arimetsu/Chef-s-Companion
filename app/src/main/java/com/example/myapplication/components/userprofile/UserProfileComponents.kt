package com.example.myapplication.components.userprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.R

data class MenuOption(
    val title: String,
    val onClick: () -> Unit,
    val isDivider: Boolean = false,
    val isHighlighted: Boolean = false
)

@Composable
fun ProfileMenuButton(
    onMenuClick: () -> Unit = {},
    menuOptions: List<MenuOption>
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        // Menu button
        IconButton(
            onClick = {
                showMenu = true
                onMenuClick()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.menu_icon),
                contentDescription = "Menu",
                tint = Color(0xFF2B5D2A), // Dark green color
                modifier = Modifier.size(18.dp)
            )
        }

        // Dropdown menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .width(280.dp)
                .background(Color.White)
                .clip(RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp)
        ) {
            // Header with "Settings" title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.menu_icon),
                        contentDescription = null,
                        tint = Color(0xFF2B5D2A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2B5D2A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.LightGray, thickness = 1.dp)

            // Menu options
            menuOptions.forEach { option ->
                if (option.isDivider) {
                    Divider(color = Color.LightGray, thickness = 1.dp)
                } else {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.title,
                                fontSize = 16.sp,
                                color = if (option.isHighlighted) Color(0xFF2B5D2A) else Color.Black,
                                fontWeight = if (option.isHighlighted) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            option.onClick()
                            showMenu = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirmLogout: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Log Out",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to log out of your account?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cancel")
                    }

                    // Confirm logout button
                    Button(
                        onClick = onConfirmLogout,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)  // Red color for logout
                        )
                    ) {
                        Text("Log Out")
                    }
                }
            }
        }
    }
}