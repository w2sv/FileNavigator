plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":data"))
    implementation(project(":domain"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.viewmodel)

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.google.guava)

    implementation(libs.google.hilt)
    ksp(libs.google.hilt.compiler)

    implementation(libs.simplestorage)

    // ---------------
    // Test

    testImplementation(project(":test"))
}
