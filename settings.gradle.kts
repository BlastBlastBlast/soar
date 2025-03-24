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
        maven {
            url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-all/")
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)  // This will prevent repositories in build.gradle
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-all/")
        }
    }
}

rootProject.name = "in2000_met2025_team21"
include(":app")