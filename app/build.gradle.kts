// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL
// Unauthorized use, reproduction, or distribution is prohibited.

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.lifeeasy.agent"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.lifeeasy.agent"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // SceneView 2.3.0 matches this project's Kotlin 2.0.21 / JVM 17 toolchain.
    implementation("io.github.sceneview:sceneview:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:security"))
    implementation(project(":feature:agent"))
    implementation(project(":feature:memory"))
    implementation(project(":feature:voice"))
    implementation(project(":feature:docs"))
    implementation(project(":feature:monitor"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:settings"))
}
