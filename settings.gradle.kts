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

        // Mapbox Maven repository
        maven {
            url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-all/")
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            // Optional: If your Mapbox account requires authentication for downloads,
            // uncomment and configure the authentication block below:
            /*
            authentication {
                basic(BasicAuthentication)
            }
            credentials {
                username = "mapbox"
                // You can reference a token from your environment or a local properties file
                password = System.getenv("MAPBOX_DOWNLOADS_TOKEN") ?: ""
            }
            */
        }
    }
}

rootProject.name = "in2000_met2025_team21"
include(":app")
