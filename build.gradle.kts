// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // https://developer.android.com/build/releases/gradle-plugin#updating-plugin
    alias(libs.plugins.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // https://developer.android.com/develop/ui/compose/compiler
    alias(libs.plugins.kotlin.compose) apply false

    // https://developer.android.com/jetpack/androidx/releases/room#declaring_dependencies
    // https://developer.android.com/build/migrate-to-ksp#add-ksp
    alias(libs.plugins.google.devtools.ksp) apply false
    // https://developer.android.com/jetpack/androidx/releases/room#gradle-plugin
    alias(libs.plugins.room) apply false

    // https://firebase.google.com/docs/android/setup#add-config-file
    alias(libs.plugins.google.services) apply false
}
