package com.example.version.plugin

object Versions {
    val core_ktx = "1.1.0"
    val kotlin = "1.6.10"
    val appcompat = "1.2.0-alpha02"
    val material = "1.0.0"
    val constraint_layout = "2.0.0-alpha2"
    val junit = "4.12"
    val ext_junit = "1.1.2"
    val espresso_core = "3.2.0"
    val fragment = "1.2.0"
}

object Deps {
    val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    val app_compat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val material = "com.google.android.material:material:${Versions.material}"
    val constraint_layout = "androidx.constraintlayout:constraintlayout:${Versions.constraint_layout}"
    val junit = "junit:junit:${Versions.junit}"
    val ext_junit = "androidx.test.ext:junit:${Versions.ext_junit}"
    val espresso_core = "androidx.test.espresso:espresso-core:${Versions.espresso_core}"

    val fragment_runtime = "androidx.fragment:fragment:${Versions.fragment}"
    val fragment_runtime_ktx = "androidx.fragment:fragment-ktx:${Versions.fragment}"
    val fragment_testing = "androidx.fragment:fragment-testing:${Versions.fragment}"

}