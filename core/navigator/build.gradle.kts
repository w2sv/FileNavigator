plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.compose)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android { buildFeatures.viewBinding = true }

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.designsystem)
    implementation(projects.core.domain)
    implementation(projects.core.navigatorDomain)
    implementation(projects.core.navigatorNotifications)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)

    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.slimber)

    implementation(libs.google.guava)

    implementation(libs.w2sv.simplestorage)

    // ==============
    // Test
    // ==============

    testImplementation(projects.core.test)
}
