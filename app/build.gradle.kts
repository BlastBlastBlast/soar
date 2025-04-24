val ktor_version: String by project

plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.android") version "2.1.20"
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
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/INDEX.LIST",
                "META-INF/third-party-licenses/erddap/COHORT_LICENSE",
                "META-INF/third-party-licenses/edal/LICENSE",
                "META-INF/DEPENDENCIES",
                "META-INF/third-party-licenses/junit/LICENSE",
                "META-INF/third-party-licenses/gretty/LICENSE",
                "META-INF/third-party-licenses/NOAA_LICENSE"
            )
            println("Exclusions: $excludes") // Debug log to verify exclusions
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
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //ksp
    ksp("com.google.dagger:hilt-compiler:2.55")

    //Ktor
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-serialization:$ktor_version")
    implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-client-json:$ktor_version")
    implementation("io.ktor:ktor-client-logging-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

    //implementation("io.ktor:ktor-client-serialization-native:$ktor_version")
    implementation(libs.kotlinx.serialization.json.v132)

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.37.2")

    // Mapbox
    implementation("com.mapbox.extension:maps-compose:11.11.0")
    implementation("com.mapbox.maps:android:11.11.0")

    // Icons
    implementation(libs.androidx.material.icons.extended)

    // kotlin reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // apache commons
    implementation(libs.commons.math3)

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

    implementation(kotlin("stdlib"))

    implementation("org.aoache.commons:commons-math3:3.6.1")

    //netcdf and dependencies
    implementation(libs.cdm.core)// Core library
    implementation(libs.grib) // GRIB1 & GRIB2 support
    implementation(libs.guava.v3100android)
    implementation(libs.listenablefuture)

//    constraints {
//        implementation("com.google.guava:guava:31.0.1-android")
//        implementation("com.google.code.findbugs:jsr305:3.0.2")
//        implementation("com.google.guava:listenablefuture:1.0")
//    }

    // ND4J CPU Backend for linear algebra
//    implementation(libs.nd4j.native.platform) {
//        exclude(group = "com.google.guava", module = "guava")
//        //exclude(group = "org.nd4j", module = "protobuf")
//        exclude(group = "com.google.code.findbugs", module = "jsr305")
//        exclude(group = "com.google.guava", module = "listenablefuture")
//    }

    //database dependencies
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
}

//TEST LINE