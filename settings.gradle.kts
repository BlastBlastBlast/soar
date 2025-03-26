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
    repositories {
        google()
        mavenCentral()

        //Netcdf Maven repository
        maven{
            url = uri("https://artifacts.unidata.ucar.edu/repository/unidata-all/")
        }

        // Mapbox Maven repository
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
        }
    }
}

rootProject.name = "in2000_met2025_team21"
include(":app")
