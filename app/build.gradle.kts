plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35


    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // Use latest version
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.storage)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("androidx.compose.ui:ui:1.5.3")
    implementation ("androidx.compose.material:material:1.5.3")
    implementation ("androidx.compose.ui:ui-tooling-preview:1.5.3")
    debugImplementation ("androidx.compose.ui:ui-tooling:1.5.3")
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.7.6")
    implementation ("androidx.compose.ui:ui:1.3.0")
    implementation ("androidx.compose.ui:ui-text:1.3.0")
    implementation("androidx.compose.ui:ui:1.4.0")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.ui:ui-text:1.5.0")
    implementation ("androidx.compose.foundation:foundation:1.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.google.code.gson:gson:2.10.1")

    // Import the Firebase BoM
    implementation (platform("com.google.firebase:firebase-bom:32.7.0"))

    // Add Firebase dependencies without version numbers

    implementation ("com.google.firebase:firebase-firestore:25.0.0")
    implementation("com.google.firebase:firebase-storage-ktx")


    // Google Log in
    implementation("com.google.android.gms:play-services-auth:21.1.1") // Check for latest version

}