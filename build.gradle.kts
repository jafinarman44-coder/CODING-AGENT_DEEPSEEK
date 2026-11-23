// LIFEEASY — Copyright (c) 2026 MAHADI HASAN JEWEL
// Unauthorized use, reproduction, or distribution is prohibited.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    group = "com.lifeeasy.agent"
    version = "1.0.0"
}
