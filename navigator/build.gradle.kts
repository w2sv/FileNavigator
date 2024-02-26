plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    id("kotlin-parcelize")
}

dependencies {
    implementation(projects.domain)
    implementation(projects.common)

    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.viewmodel)

    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.google.guava)

    implementation(libs.simplestorage)

    // ---------------
    // Test

    testImplementation(projects.test)
}
