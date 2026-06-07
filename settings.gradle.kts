pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "app"

include(":app")

// Core modules
include(":core-ui")
include(":core-data")
include(":core-database")
include(":core-network")
include(":core-auth")

// Sync
include(":sync")

// Feature modules
include(":feature-auth")
include(":feature-coach")
include(":feature-roster")
include(":feature-rankings")
include(":feature-analytics")
include(":feature-profile")
