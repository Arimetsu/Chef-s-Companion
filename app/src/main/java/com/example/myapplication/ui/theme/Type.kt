package com.example.myapplication.ui.theme

import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R


val montserratFamily = FontFamily (
    Font(R.font.montserrat_regular, FontWeight.Normal),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_semibold, FontWeight.SemiBold)
)

val latoFamily = FontFamily (
    Font(R.font.lato_light, FontWeight.Light),
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold)
)

// Set of Material typography styles to start with

val latoFont = FontFamily(
    Font(R.font.lato_regular),
    Font(R.font.lato_bold, FontWeight.Bold)
)

val Typography = Typography(
    //Mali yung parameter ko baliktad yung title tas yung headline ong di ko na palitan matrabaho
    titleLarge = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),

    titleSmall = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
            ),

    titleMedium = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),

    headlineMedium = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),

    headlineSmall = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    ),



    bodyMedium = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    bodySmall = TextStyle(
        fontFamily = montserratFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),

    labelSmall = TextStyle(
        fontFamily = latoFamily,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp
    ),

    labelMedium =  TextStyle(
        fontFamily = latoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
            ),




    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
