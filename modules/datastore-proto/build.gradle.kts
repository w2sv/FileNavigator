plugins {
    id("com.android.lint")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.protobuf)
}

// Align compilation target with android modules, which target jdk 17 via convention plugin
kotlin { jvmToolchain(17) }

// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }
    generateProtoTasks {
        all().configureEach {
            builtins {
                named("java") { option("lite") }
                register("kotlin") { option("lite") }
            }
        }
    }
}

dependencies {
    api(libs.protobuf.kotlin.lite)
}
