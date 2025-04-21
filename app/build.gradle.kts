plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.room)

    alias(libs.plugins.google.services)
}

android {
    namespace = "gr.christianikatragoudia.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "gr.christianikatragoudia.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 11
        versionName = "1.4.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {

    //implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform(libs.compose.bom))
    //implementation("androidx.compose.foundation:foundation-layout-android:1.6.8")
    implementation(libs.compose.material3)
    //implementation("androidx.compose.ui:ui")
    //implementation("androidx.compose.ui:ui-graphics")
    implementation(libs.compose.ui.tooling.preview)
    //implementation("androidx.core:core-ktx:1.13.1")
    //implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    //implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

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
