plugins {
    id("com.android.application")
}

android {
    namespace = "com.vodka.cheto"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.vodka.cheto"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
}
