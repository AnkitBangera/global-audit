plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

configurations.all {
    resolutionStrategy {
        // Force patched transitive versions for security (addresses Snyk findings)
        // Netty family
        force("io.netty:netty-common:4.1.129.Final")
        force("io.netty:netty-codec-http:4.1.129.Final")
        force("io.netty:netty-codec-http2:4.1.129.Final")
        force("io.netty:netty-handler:4.1.129.Final")
        force("io.netty:netty-buffer:4.1.129.Final")
        force("io.netty:netty-transport:4.1.129.Final")

        // Protobuf
        force("com.google.protobuf:protobuf-java:3.25.5")
        force("com.google.protobuf:protobuf-java-util:3.25.5")

        // Guava (Android variant)
        force("com.google.guava:guava:33.4.0-android")

        // Kotlin stdlib alignment to avoid older vulnerable stdlib in transitive deps
        force("org.jetbrains.kotlin:kotlin-stdlib:2.1.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk7:2.1.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.20")
        force("org.jetbrains.kotlin:kotlin-stdlib-common:2.1.20")
    }
}

android {
    namespace = "com.landmarkgroup.globalaudit"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.landmarkgroup.globalaudit"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["appAuthRedirectScheme"] = "http"
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
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.runtime.ktx)
    
    // AppAuth for ADFS/OAuth2 login
    implementation("net.openid:appauth:0.11.1")
    implementation("androidx.browser:browser:1.7.0")
    // Encrypted SharedPreferences for secure token storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // JWT decoding
    implementation("com.auth0.android:jwtdecode:2.0.2")
    
    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")
    
    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
