import java.util.Properties
import java.io.FileInputStream

val secretsPropertiesFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()
if (secretsPropertiesFile.exists()) {
    secretsProperties.load(FileInputStream(secretsPropertiesFile))
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "no.uio.ifi.in2000.met2025"
    compileSdk = 35

    defaultConfig {
        applicationId = "no.uio.ifi.in2000.met2025"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        // Expose the Mapbox token as a BuildConfig field.
        buildConfigField(
            "String",
            "MAPBOX_ACCESS_TOKEN",
            "\"${secretsProperties.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\""
        )

        // Also, generate a string resource for Mapbox.
        resValue("string", "mapbox_access_token", "\"${secretsProperties.getProperty("MAPBOX_ACCESS_TOKEN") ?: ""}\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources {
            excludes += setOf("META-INF/INDEX.LIST")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        buildConfig = true
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))


    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.appcompat)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    //ksp
    implementation("com.google.dagger:hilt-compiler:2.51.1")
    ksp("com.google.dagger:hilt-compiler:2.51.1")
    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json.v132)

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.2")

    // Mapbox
    implementation("com.mapbox.extension:maps-compose:11.10.3")
    implementation("com.mapbox.maps:android:11.10.3")

    // Removed logback dependency:
    // implementation(libs.logback.classic)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}