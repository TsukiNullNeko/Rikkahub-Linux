pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        google()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        mavenCentral()
        google()
        maven("https://jitpack.io")
        maven("https://repo.itextsupport.com/android")
    }
}

rootProject.name = "rikkahub-linux"

include(":platform")
include(":hugeicons")
include(":common")
include(":material3")
include(":highlight")
include(":document")
include(":ai")
include(":search")
include(":mediagen")
include(":oauth")
include(":workspace")
include(":speech")
include(":ui")
include(":desktop")