pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        includeBuild("build-logic")
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "FileNavigator"

include(":app")
include(":benchmarking")
include(":modules:database")
include(":modules:datastore")
include(":modules:datastore-proto")
include(":modules:designsystem")
include(":modules:domain")
include(":modules:navigator")
include(":modules:navigator-domain")
include(":modules:navigator-notifications")
include(":modules:navigator-quicktile")
include(":modules:resources")
include(":modules:shared")
include(":modules:storage")
include(":modules:test")
include(":modules:usecase")
