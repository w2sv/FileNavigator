plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.compose)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.modules.common)
    implementation(projects.modules.designsystem)
    implementation(projects.modules.domain)
    implementation(projects.modules.navigatorDomain)
    implementation(projects.modules.navigatorNotifications)
    implementation(projects.modules.resources)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.simplestorage)

    // ==============
    // Test
    // ==============

    testImplementation(projects.modules.test)
}
