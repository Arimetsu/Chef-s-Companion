package com.example.myapplication.front_end.userprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(
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
                        text = "Terms And Conditions",
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
                text = "By using our app, you agree to these Terms and Conditions. If you do not agree with any of the terms, you should discontinue use of the app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Account Registration and Security section
            TermsSection(
                title = "Account Registration and Security",
                content = "To use our services, you must create an account using an email and password or via third-party login services like Google or Facebook. You are responsible for maintaining the security and confidentiality of your account information."
            )

            // Use of the App section
            TermsSection(
                title = "Use of the App",
                content = "Our app provides meal planning, recipe management, and personalized suggestions. You agree to use the app for lawful purposes and not to engage in any activity that could harm the functionality or security of the app."
            )

            // Intellectual Property section
            TermsSection(
                title = "Intellectual Property",
                content = "All content, features, and functionality of the app, including recipes, images, and other materials, are the property of the app and are protected by intellectual property laws."
            )

            // User-Generated Content section
            TermsSection(
                title = "User-Generated Content",
                content = "You retain ownership of the recipes you submit to the app but grant us a license to use, modify, and display your content within the app. You are solely responsible for any content you upload, including the accuracy and legality of your recipes."
            )

            // Limitation of Liability section
            TermsSection(
                title = "Limitation of Liability",
                content = "We are not responsible for any damages or losses that may occur from using the app, including but not limited to issues related to recipe inaccuracies, nutritional information, or other content provided."
            )

            // Termination section
            TermsSection(
                title = "Termination",
                content = "We reserve the right to suspend or terminate your access at our discretion if you violate any of the terms of service. You can also terminate your account at any time by contacting customer support."
            )

            // Governing Law section
            TermsSection(
                title = "Governing Law",
                content = "These Terms and Conditions are governed by the laws of [your jurisdiction], and any disputes will be subject to the exclusive jurisdiction of the courts in [your jurisdiction]."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TermsSection(
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
fun TermsAndConditionsScreenPreview() {
    MaterialTheme {
        TermsAndConditionsScreen(
            onBackClick = {} // No-op for preview
        )
    }
}