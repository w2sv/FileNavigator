plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
}

dependencies {
    implementation(projects.modules.navigatorDomain)
    implementation(projects.modules.domain)
    implementation(projects.modules.coreAndroid)
    implementation(projects.modules.resources)
    implementation(projects.modules.storage)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.documentfile)
    implementation(libs.w2sv.androidutils.core)

    testImplementation(libs.bundles.unitTest)
}
