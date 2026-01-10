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
include(":core:common")
include(":core:datastore")
include(":core:database")
include(":core:designsystem")
include(":core:domain")
include(":core:navigator")
include(":core:navigator-domain")
include(":core:navigator-notifications")
include(":core:test")
