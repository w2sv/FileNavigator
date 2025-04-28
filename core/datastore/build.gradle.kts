import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.filenavigator.library)
    alias(libs.plugins.filenavigator.hilt)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
}

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                id("kotlin") // Enables kotlin DSL
            }
        }
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.domain)

    implementation(libs.androidx.core)
    implementation(libs.protobuf.kotlin.lite)

    implementation(libs.w2sv.kotlinutils)
    implementation(libs.w2sv.datastoreutils.preferences)
    implementation(libs.w2sv.datastoreutils.datastoreflow)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.slimber)

    testImplementation(projects.core.test)
}
