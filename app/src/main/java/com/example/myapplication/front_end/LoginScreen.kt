package com.example.myapplication.front_end

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import androidx.compose.ui.unit.sp

import androidx.compose.ui.text.font.FontFamily

import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import androidx.compose.ui.text.font.Font

var latoFontLI = FontFamily(
    Font(R.font.lato_regular),
    Font(R.font.lato_bold, FontWeight.Bold)
)

@Composable
fun AccountSuccessfullyCreated(navController: NavController) {
    val backgroundColor = Color(26 / 255f, 77 / 255f, 46 / 255f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Account Created!",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = latoFontLI,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(R.drawable.whitelogo),
            contentDescription = "Chef's Companion",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Chef's Companion",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = latoFontLI,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                    //
                navController.navigate("signIn")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),

            ) {
            Text(text = "Let's Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = latoFontLI)

        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {

    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        ) {
        Spacer(modifier = Modifier.height(50.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Image(
                    painter = painterResource(R.drawable.logo_withname),
                    contentDescription = "Chef's Companion",
                    modifier = Modifier.size(100.dp)
                )
                // Title
                Text(
                    text = "Sign In",
                    fontSize = 33.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = latoFontLI
                )
            }

        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "Hi! Welcome back, you’ve been missed",
                fontSize = 12.sp,
                fontFamily = latoFontLI
            )
        }

        Spacer(modifier = Modifier.height(60.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {


            //Username
            Text(
                text = "Email:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFontLI,

                )

            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && email.isBlank()) {
                Text(
                    text = "Incorrect Email/Username!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFontLI,
                )
            }


        }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("Ex. examplechef09") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = latoFontLI,

                    ),
                shape = RoundedCornerShape(10.dp),
                isError = isError && email.isBlank(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                    unfocusedBorderColor = Color.Gray, // Border color when unfocused
                    errorBorderColor = Color.Red       // Border color when there's an error
                )
            )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            //password
            Text(
                text = "Password:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFontLI
            )

            //error message
            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && password.isBlank()) {
                Text(
                    text = "Incorrect Password!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFontLI,
                )
            }

        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("******") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off),
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = latoFont,
            ),
            shape = RoundedCornerShape(10.dp),
            isError = isError && password.isBlank(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                unfocusedBorderColor = Color.Gray, // Border color when unfocused
                errorBorderColor = Color.Red       // Border color when there's an error
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ){
            Text(
                text = "Forgot Password?",
                color = Color(26, 77, 46),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                fontFamily = latoFontLI,
                modifier = Modifier.clickable(
                    onClick = {
                        navController.navigate("forgotPassword")
                    }
                )
                )
        }

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                isError = email.isBlank() || password.isBlank()

                if(!isError){

                    navController.navigate("home")
                    //Process sa sign in and mag llog in na siya

                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),

            ) {
            Text(text = "Sign In", fontSize = 18.sp, color = Color.White, fontFamily = latoFont)

        }

        Spacer(modifier = Modifier.height(50.dp))

        // Social Sign-In Options
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth() // Make the Row fill the width
        ) {
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
            Text(
                text = "Or sign in with",
                color = Color.Gray,
                fontFamily = latoFontLI, // or your custom font
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
        }
        Spacer(modifier = Modifier.height(18.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(onClick = { /* Facebook Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Size of the border
                        .border(
                            1.dp,
                            Color.LightGray,
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.icons8_facebook_logo_192), // Your colored Facebook icon
                        contentDescription = "Facebook",
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)// Set the size of the image

                    )
                }


            }
            IconButton(onClick = { /* Google Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Size of the border
                        .border(
                            1.dp,
                            Color.LightGray,
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.icons8_google_144), // Your colored Google icon
                        contentDescription = "Google",
                        modifier = Modifier
                            .size(30.dp) // Set the size of the image
                            .align(Alignment.Center)

                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(22.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row{

                Text(text = "Don’t have an account? ",
                    fontFamily = latoFontLI)
                Text(
                    text = "Sign up",
                    color = Color(26, 77, 46),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFontLI,
                    modifier = Modifier.clickable(
                        onClick = {
                            navController.navigate("signUp")
                        }
                    )
                )
            }
        }

    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController){
    var email by remember { mutableStateOf("")}
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        ) {

        Spacer(modifier = Modifier.height(50.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "Forgot Password",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFontLI
            )

        }
        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "Find your account first!",
                fontSize = 12.sp,
                fontFamily = latoFontLI
            )

        }

        Spacer(modifier = Modifier.height(80.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {


            //Email
            Text(
                text = "Email:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFontLI,

                )

            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && email.isBlank()) {
                Text(
                    text = "Email Not Found!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFontLI,
                )
            }


        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Ex. examplechef09") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = latoFontLI,

                ),
            shape = RoundedCornerShape(10.dp),
            isError = isError && email.isBlank(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                unfocusedBorderColor = Color.Gray, // Border color when unfocused
                errorBorderColor = Color.Red       // Border color when there's an error
            )
        )

        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                isError = email.isBlank()

                if(!isError){

                    //Process sa sign in and mag llog in na siya
                    navController.navigate("verification")
                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),

            ) {
            Text(text = "Send", fontSize = 18.sp, color = Color.White, fontFamily = latoFont)

        }

        Spacer(modifier = Modifier.height(50.dp))

        // Social Sign-In Options
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth() // Make the Row fill the width
        ) {
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
            Text(
                text = "Or sign in with",
                color = Color.Gray,
                fontFamily = latoFontLI, // or your custom font
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(onClick = { /* Facebook Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Size of the border
                        .border(
                            1.dp,
                            Color.LightGray,
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.icons8_facebook_logo_192), // Your colored Facebook icon
                        contentDescription = "Facebook",
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)// Set the size of the image

                    )
                }


            }
            IconButton(onClick = { /* Google Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Size of the border
                        .border(
                            1.dp,
                            Color.LightGray,
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.icons8_google_144), // Your colored Google icon
                        contentDescription = "Google",
                        modifier = Modifier
                            .size(30.dp) // Set the size of the image
                            .align(Alignment.Center)

                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row{

                Text(text = "Don’t have an account? ",
                    fontFamily = latoFontLI)
                Text(
                    text = "Sign up",
                    color = Color(26, 77, 46),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFontLI,
                    modifier = Modifier.clickable(
                        onClick = {
                            navController.navigate("signUp")
                        }
                    )
                )
            }
        }

    }
}

@Composable
fun VerificationScreen(navController: NavController){
    var code by remember { mutableStateOf("")}
    var isError by remember { mutableStateOf(false) }

    var isResendEnabled by remember { mutableStateOf(true) }  // Whether Resend button is enabled
    var timer by remember { mutableStateOf(0) }


    if (!isResendEnabled) {

        //dito yung backend na magssend ulit ng code

        LaunchedEffect(timer) {
            while (timer > 0) {
                delay(1000)
                timer -= 1
            }
            isResendEnabled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {

        Spacer(modifier = Modifier.height(50.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "Verification",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFontLI
            )

        }
        Spacer(modifier = Modifier.height(60.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "Enter Your Verification Code",
                fontSize = 12.sp,
                fontFamily = latoFontLI
            )

        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {


            //Email
            Text(
                text = "",
                )

            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && code.isBlank()) {
                Text(
                    text = "Incorrect Code!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFontLI,
                )
            }


        }
        OutlinedTextField(
            value = code,
            onValueChange = {newCode ->
                // Ensure the new value is a valid integer or an empty string
                if (newCode.isEmpty() || newCode.all { it.isDigit() }) {
                    code = newCode
                } },
            label = { Text("Code") },
            placeholder = { Text("22993344") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = latoFont,

                ),
            shape = RoundedCornerShape(10.dp),
            isError = isError && code.isBlank(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                unfocusedBorderColor = Color.Gray, // Border color when unfocused
                errorBorderColor = Color.Red       // Border color when there's an error
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Text(
                text = "Did not receive our email? ",
                fontSize = 11.sp,
                fontFamily = latoFont
            )
            if (isResendEnabled) {
                Text(
                    text = "Resend",
                    color = Color(26, 77, 46),
                    fontSize = 11.sp,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFont,
                    modifier = Modifier.clickable {
                        isResendEnabled = false
                        timer = 180  // Reset timer to 3 minutes
                    }
                )
            } else {
                // Display the remaining time in seconds
                Text(
                    text = "Resend available in ${timer / 60}:${timer % 60}",
                    color = Color(26, 77, 46),
                    fontSize = 11.sp,
                    fontFamily = latoFont
                )
            }

        }
        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = {
                isError = code.isBlank()

                if(!isError){

                    //Process sa sign in and mag llog in na siya
                    navController.navigate("newPassword")
                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),

            ) {
            Text(text = "Verify",
                fontSize = 18.sp,
                color = Color.White,
                fontFamily = latoFont)

        }

        Spacer(modifier = Modifier.height(50.dp))

        // Social Sign-In Options
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth() // Make the Row fill the width
        ) {
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
            Text(
                text = "Or sign in with",
                color = Color.Gray,
                fontFamily = latoFontLI, // or your custom font
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {

            IconButton(onClick = { /* Facebook Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Size of the border
                        .border(
                            1.dp,
                            Color.LightGray,
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.icons8_facebook_logo_192), // Your colored Facebook icon
                        contentDescription = "Facebook",
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.Center)// Set the size of the image

                    )
                }


            }
            IconButton(onClick = { /* Google Sign-In */ }) {
                Box(
                    modifier = Modifier
                        .size(48.dp) // Size of the border
                        .border(
                            1.dp,
                            Color.LightGray,
                            RoundedCornerShape(50.dp)
                        ) // Light border with rounded corners
                        .clip(RoundedCornerShape(50.dp)) // Clip the border to rounded corners
                ) {
                    Image(
                        painter = painterResource(R.drawable.icons8_google_144), // Your colored Google icon
                        contentDescription = "Google",
                        modifier = Modifier
                            .size(30.dp) // Set the size of the image
                            .align(Alignment.Center)

                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row{

                Text(text = "Don’t have an account? ",
                    fontFamily = latoFontLI)
                Text(
                    text = "Sign up",
                    color = Color(26, 77, 46),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFontLI,
                    modifier = Modifier.clickable(
                        onClick = {
                            navController.navigate("signUp")
                        }
                    )
                )
            }
        }

    }
}

@Composable
fun NewPasswordScreen(navController: NavController){

    var isError by remember { mutableStateOf(false) }
    var isPasswordMismatch by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordVisible_ by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "New Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFontLI
            )

        }
        Spacer(modifier = Modifier.height(80.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            //password
            Text(
                text = "Password:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFont
            )

            //error message
            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && password.isBlank()) {
                Text(
                    text = "Input Valid Password!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFont,
                )
            }

        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("******") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible) R.drawable.visibility else R.drawable.visibility_off),
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = latoFont,
            ),
            shape = RoundedCornerShape(10.dp),
            isError = isError && password.isBlank(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                unfocusedBorderColor = Color.Gray, // Border color when unfocused
                errorBorderColor = Color.Red       // Border color when there's an error
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            //Confirm Password
            Text(
                text = "Confirm Password:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFont
            )

            //error message
            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isPasswordMismatch) {
                Text(
                    text = "Password Do Not Match!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFont,
                )
            }

        }

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            placeholder = { Text("********") },
            trailingIcon = {
                IconButton(onClick = { passwordVisible_ = !passwordVisible_ }) {
                    Icon(
                        painter = painterResource(id = if (passwordVisible_) R.drawable.visibility else R.drawable.visibility_off),
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible_) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = latoFont,

                ),
            shape = RoundedCornerShape(10.dp),
            isError = isError || isPasswordMismatch,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(26, 77, 46),  // Border color when focused
                unfocusedBorderColor = Color.Gray, // Border color when unfocused
                errorBorderColor = Color.Red       // Border color when there's an error
            )
        )


        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                isError =  password.isBlank() || confirmPassword.isBlank()
                isPasswordMismatch = confirmPassword != password

                if(!isError && !isPasswordMismatch ){

                    //Process sa sign in and mag llog in na siya
                    navController.navigate("passwordChangeSuccessfully")
                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),

            ) {
            Text(text = "Submit", fontSize = 18.sp, color = Color.White, fontFamily = latoFont)

        }


    }
}

@Composable
fun PasswordChangeSuccessfullyScreen(navController: NavController){
    val backgroundColor = Color(26 / 255f, 77 / 255f, 46 / 255f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Password Changed!",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = latoFontLI,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(80.dp))

        Image(
            painter = painterResource(R.drawable.whitelogo),
            contentDescription = "Chef's Companion",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Chef's Companion",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = latoFontLI,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(50.dp))
        Button(
            onClick = {
                //
                navController.navigate("signIn")
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),

            ) {
            Text(text = "Sign In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontFamily = latoFontLI)

        }
    }
}

