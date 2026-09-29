plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.oxplayer"
    compileSdk = 36 // Android 16

    defaultConfig {
        applicationId = "com.example.oxplayer"
        minSdk = 24
        targetSdk = 36 // Android 16
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            // Populated by the GitHub Actions release workflow via -P flags.
            // Locally these stay null and Android Studio falls back to its
            // own debug signing when you just hit Run.
            val storeFilePath = project.findProperty("android.injected.signing.store.file") as String?
            val storePwd = project.findProperty("android.injected.signing.store.password") as String?
            val keyAliasVal = project.findProperty("android.injected.signing.key.alias") as String?
            val keyPwd = project.findProperty("android.injected.signing.key.password") as String?

            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = storePwd
                keyAlias = keyAliasVal
                keyPassword = keyPwd
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core VLC engine - supports almost every codec/container out of the box
    implementation("org.videolan.android:libvlc-all:3.6.0")

    // Compose
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // Icons for gesture/player controls
    implementation("androidx.compose.material:material-icons-extended:1.7.3")
}
