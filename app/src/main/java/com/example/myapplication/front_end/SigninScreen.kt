package com.example.myapplication.front_end

import androidx.compose.foundation.Image
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
import androidx.compose.material.Text
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.material3.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField





val latoFont = FontFamily(
    Font(R.font.lato_regular),
    Font(R.font.lato_bold, FontWeight.Bold)
)



@Composable
fun CreateAccountScreen(navController: NavController){
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordVisible_ by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isPasswordTouched by remember { mutableStateOf(false) }

    var isError by remember { mutableStateOf(false) }
    var isPasswordMismatch by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var isEmailTouched by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),

        ) {
        Spacer(modifier = Modifier.height(18.dp))
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
                    modifier = Modifier.size(75.dp)
                )

                Spacer(modifier = Modifier.width(8.dp)) // Use width for horizontal spacing

                // Title
                Text(
                    text = "Create Account",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = latoFont
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center // Center the content of the Box
        ) {
            Text(
                text = "Fill your information below or register with your social account.",
                fontSize = 9.sp,
                fontFamily = latoFont
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {


            //Username
            Text(
                text = "Username:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFont,

                )

            //error message
            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && username.isBlank()) {
                Text(
                    text = "Input Valid Username!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFont,
                )
            }

        }
        OutlinedTextField(
            value = username,
            onValueChange = {  if (it.length <= 13) { // Restrict input to 13 characters
                username = it
            }
                isError = username.length > 13 },
            label = { Text("Username") },
            placeholder = { Text("Ex. examplechef09") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = FontFamily.Default // Change to your desired font
            ),
            shape = RoundedCornerShape(10.dp),
            isError = isError,
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
            //Email

            Text(
                text = "Email:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFont
            )

            //error message
            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isError && email.isBlank()) {
                Text(
                    text = "Input Valid Email!",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFont,
                )
            }
            else if (!isValidEmail(email) && isEmailTouched){
                Text(
                    text = "Invalid Email Format",
                    color = Color.Red, // Make the message red
                    fontSize = 12.sp,
                    fontFamily = latoFont,
                )
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it
                isEmailTouched = true},
            label = { Text("Email") },
            placeholder = { Text("example@gmail.com") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = latoFont,

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
            else if (!isPasswordValid && isPasswordTouched){
                Text(
                    text = "Password must be 8-12 characters",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontFamily = latoFont,
                )
            }

        }

        OutlinedTextField(
            value = password,
            onValueChange = { newPassword ->
                password = newPassword
                isPasswordValid = newPassword.length in 8..12
                isPasswordTouched = true },
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
                fontFamily = latoFont // Change to your desired font
            ),
            shape = RoundedCornerShape(10.dp),
            isError = isError || !isPasswordValid && isPasswordTouched,
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
            //Confirm Password
            Text(
                text = "Confirm Password:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = latoFont
            )

            //error message
            Spacer(modifier = Modifier.weight(1f)) // Add space between the label and the message

            if (isPasswordMismatch && confirmPassword.isBlank()) {
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
            isError = isError && confirmPassword.isBlank() || isPasswordMismatch,
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
        ){
            Checkbox(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    showError = !it
                }
            )
            Text(text = "Agree with ", fontFamily = latoFont)
            Text(
                text = "Terms & Conditions",
                color = Color(26, 77, 46),
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                fontFamily = latoFont,
                modifier = Modifier.clickable{
                    showModal = true
                }
            )
        }
        //error
        if (showError) {
            Text(
                text = "Read and Agree to our Terms & Conditions",
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isError = username.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() || !isValidEmail(email)
                isPasswordMismatch = confirmPassword != password
                showError = isChecked.not()

                if(!isError && !isPasswordMismatch && !showError){

                    //Process sa sign in and mag llog in na siya

                    navController.navigate("emailVerification")
                }


            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),

        ) {
            Text(text = "Sign Up", fontSize = 18.sp, color = Color.White, fontFamily = latoFont)

        }

        Spacer(modifier = Modifier.height(16.dp))

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
                fontFamily = FontFamily.Default, // or your custom font
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(
                color = Color.Gray,
                modifier = Modifier.weight(1f), // This works because it's in a Row
                thickness = 1.dp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
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

        }
        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row{

                Text(text = "Already have an account?  ", fontFamily = latoFont)
                Text(
                    text = "Sign in",
                    color = Color(26, 77, 46),
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    fontFamily = latoFont,
                    modifier = Modifier.clickable(
                        onClick = {
                            navController.navigate("signIn")
                        }
                    )
                )
            }
        }
    }

    if (showModal) {
        TermsAndConditionsScreen(
            onDismiss = { showModal = false } // Close the modal when dismissed
        )
    }

}
// Terms and Condition
@Composable
fun TermsAndConditionsScreen(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Close button (X icon)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onDismiss() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Terms and Conditions text
                Text(
                    text = "Terms & Conditions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontFamily = latoFont
                )
                // term 1
                Text(
                    text = "1. Acceptance of Terms",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 5.dp),
                    fontFamily = latoFont
                )

                Text(
                    text = "By accessing or using The Chef’s Companion (the \"Service\"), you agree to comply with these Terms & Conditions (\"T&C\"). If you do not agree, refrain from using the Service.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 5.dp),
                    fontFamily = latoFont
                )

            }
        }
    }
}

@Composable
fun EmailVerificationScreen(navController: NavController){
    var code by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    // Timer states
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

    Column (
        modifier = Modifier
            .fillMaxSize() // Fills the screen
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ){
        Box(
            modifier = Modifier
                .size(75.dp) // Size of the border
                .border(
                    1.dp,
                    Color(26, 77, 46),
                    RoundedCornerShape(100.dp)
                ) // Light border with rounded corners
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Chef's Companion",
                modifier = Modifier.size(100.dp)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = "Email Verification",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = latoFont
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Enter the code we have sent you in your email address.",
            fontSize = 11.sp,
            fontFamily = latoFont
        )

        Spacer(modifier = Modifier.height(50.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(), // Adds padding to the left
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter Code Here:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = latoFont
                )
                Spacer(modifier = Modifier.weight(1f))

                if (isError && code.isBlank()) {
                    Text(
                        text = "Input Correct Code!",
                        color = Color.Red, // Make the message red
                        fontSize = 10.sp,
                        fontFamily = latoFont,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))

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
        Spacer(modifier = Modifier.height(100.dp))


        Button(
            onClick = {
                isError = code.isEmpty()
                if (!isError){
                    navController.navigate("accountSuccessfully")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(26, 77, 46)),

            ) {
            Text(text = "Verify", fontSize = 18.sp, color = Color.White, fontFamily = latoFont)

        }
    }
}

fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

