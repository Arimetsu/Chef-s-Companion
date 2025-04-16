package com.example.myapplication.front_end.userprofile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Introduction
            Text(
                text = "We respect your privacy and are committed to protecting your personal information. This Privacy Policy outlines how we collect, use, and safeguard your data when using our app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Information Collection section
            PrivacyPolicySection(
                title = "Information Collection",
                content = "We collect personal information, including your email, password, and profile details when you register or log into our app. Additionally, we may collect information regarding your usage of the app, such as meal preferences and interactions with features."
            )

            // How We Use Your Information section
            PrivacyPolicySection(
                title = "How We Use Your Information",
                content = "Your information is used to provide personalized features such as meal planning, recipe suggestions, and profile management. We may also use your data for internal purposes like improving the app's functionality and sending notifications about your meal plans and recipe suggestions."
            )

            // Data Security section
            PrivacyPolicySection(
                title = "Data Security",
                content = "We implement a variety of security measures to protect your personal information from unauthorized access or alteration. This includes encryption and secure login methods."
            )

            // Sharing of Information section
            PrivacyPolicySection(
                title = "Sharing of Information",
                content = "We do not sell or rent your personal information to third parties. We may share data with third-party service providers for functionality like authentication and meal suggestions, but these parties are obligated to protect your data."
            )

            // User Rights section
            PrivacyPolicySection(
                title = "User Rights",
                content = "You have the right to access, update, or delete your account and profile information. You can manage your preferences and change your password anytime through the app settings."
            )

            // Cookies section
            PrivacyPolicySection(
                title = "Cookies",
                content = "We use cookies to improve your experience and to analyze app usage trends. By using our app, you consent to the use of cookies in accordance with this Privacy Policy."
            )

            // Changes to the Privacy Policy section
            PrivacyPolicySection(
                title = "Changes to the Privacy Policy",
                content = "We may update our Privacy Policy from time to time. All changes will be communicated within the app and will be effective immediately upon posting."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacyPolicySection(
    title: String,
    content: String
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PrivacyPolicyScreenPreview() {
    MaterialTheme {
        PrivacyPolicyScreen(
            onBackClick = {} // No-op for preview
        )
    }
}