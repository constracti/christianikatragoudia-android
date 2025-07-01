plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.room)

    alias(libs.plugins.google.services)
}

android {
    namespace = "gr.christianikatragoudia.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "gr.christianikatragoudia.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 14
        versionName = "1.5.2"
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

    // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)

    // https://developer.android.com/jetpack/androidx/releases/datastore#preferences-datastore-dependencies
    implementation(libs.datastore.preferences)

    // https://developer.android.com/jetpack/androidx/releases/navigation#declaring_dependencies
    implementation(libs.navigation.compose)

    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    // https://developer.android.com/jetpack/androidx/releases/work#declaring_dependencies
    implementation(libs.work.runtime.ktx)

    // https://firebase.google.com/docs/android/setup#add-sdks
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // https://github.com/square/moshi/blob/master/README.md
    implementation(libs.moshi.kotlin)

    // https://github.com/square/retrofit/blob/trunk/retrofit-converters/moshi/README.md
    implementation(libs.converter.moshi)
}
