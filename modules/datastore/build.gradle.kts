plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

android.defaultConfig.consumerProguardFiles("consumer-proguard-rules.pro")

dependencies {
    implementation(projects.modules.coreAndroid)
    implementation(projects.modules.domain)
    implementation(projects.modules.datastoreProto)

    implementation(libs.androidx.core.ktx)
    implementation(libs.w2sv.persistedPreferences)
    implementation(libs.w2sv.androidutils.core)

    testImplementation(projects.modules.test)
}
