package com.example.myapplication.front_end.userprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaqsScreen(
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
                        text = "FAQs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(end = 18.dp)
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
            // Introduction text
            Text(
                text = "Welcome! Here, you'll find answers to common questions about using our app, from account management to meal planning features. If you can't find the information you're looking for, feel free to reach out to our support team for further assistance.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // FAQ Item 1
            FaqItem(
                number = "1",
                question = "How do I create an account?",
                answer = "You can sign up through the registration page by entering your email address, creating a password, or using third-party login services like Google or Facebook."
            )

            // FAQ Item 2
            FaqItem(
                number = "2",
                question = "How can I manage my profile?",
                answer = "You can edit your profile details, dietary restrictions, and preferences on the Profile Management page."
            )

            // FAQ Item 3
            FaqItem(
                number = "3",
                question = "Can I share my recipes?",
                answer = "Yes! You can share your recipes with other users or on social media using the Recipe Sharing page."
            )

            // FAQ Item 4
            FaqItem(
                number = "4",
                question = "How does the AI-powered recipe suggestion work?",
                answer = "The system suggests recipes based on the ingredients you input, your dietary preferences, and your past activity within the app."
            )

            // FAQ Item 5
            FaqItem(
                number = "5",
                question = "Can I delete my recipes?",
                answer = "Yes, you can edit or delete any recipe you've added in the Recipe Management section."
            )

            // FAQ Item 6
            FaqItem(
                number = "6",
                question = "How do I create a meal plan?",
                answer = "You can use the Weekly/Monthly Planner page to drag and drop meals into your meal plan for specific days."
            )

            // FAQ Item 7
            FaqItem(
                number = "7",
                question = "What should I do if I forget my password?",
                answer = "Simply go to the login page and click on \"Forgot Password\" to reset it using your registered email."
            )

            // FAQ Item 8
            FaqItem(
                number = "8",
                question = "How can I contact support?",
                answer = "If you need assistance, you can visit the Help & Support page to access FAQs or contact our support team directly."
            )

            // FAQ Item 9
            FaqItem(
                number = "9",
                question = "Is my data secure?",
                answer = "Yes, we use industry-standard security measures to protect your personal information and account details."
            )

            // FAQ Item 10
            FaqItem(
                number = "10",
                question = "How do I delete my account?",
                answer = "If you wish to delete your account, please reach out to our support team through the Help & Support page."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FaqItem(
    number: String,
    question: String,
    answer: String
) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Row(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = "$number. ",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 22.sp,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FaqsScreenPreview() {
    MaterialTheme {
        FaqsScreen(
            onBackClick = {} // No-op for preview
        )
    }
}