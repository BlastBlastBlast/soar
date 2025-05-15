# SOAR

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
- [Usage](#usage)
    - [Find Launch Windows](#find-launch-windows)
    - [Trajectory Simulator](#trajectory-simulator)
- [Navigation & UI](#navigation--ui)
- [Known Issues](#known-issues)
- [Acknowledgments](#acknowledgments)

---

## Overview
**SOAR** is an Android app developed as part of a University of Oslo student project, with the Portal Space rocketry club as our primary user group. Based on interviews and usability tests with Portalspace members, SOAR aims to help small‐scale rocketry teams:
- Determine safe launch windows by fetching and evaluating MET weather data at chosen launch sites
- Visualize hourly and daily weather summaries with “Safe / Caution / Unsafe” indicators
- Simulate rocket flight trajectories in 3D based on configurable rocket parameters

---

## Features
- **Launch-site management**
    - Drop a marker on a Mapbox map (long-press) to grab coordinates and elevation
    - Save, name, delete and revisit any number of sites
- **Weather evaluation**
    - Daily and hourly summaries from MET Locationforecast
    - Organized and detailed view of weather data
        - Extensive data at ground level
        - GRIB2 wind and temperature data up to 100hpa/~16000m AMSL(above mean sea level)
    - “Safe / Caution / Unsafe / No data” evaluation badges on each hourly time slot
    - Create and save easily switchable weather configurations, with custom threshold values for launch window evaluation
    - Flexible filtering
        - Show only safe launch windows (or any mix of Safe/Caution/Unsafe)
        - Sunrise / sunset filtering (show only launch windows for which it's sunlight outside)
- **3D trajectory simulation**
    - Define and save configurations for rocket parameters (mass, thrust, drag, parachute, launch angles, etc..)
    - Easily switch between configurations for simulation
    - Render 3D markers along the flight path at user-defined intervals, the caluclations are based on your chosen rocket parameters and interpolated weather data from a minimum of 16 data points.
    - A launch can be simulated for all launchwindows for which data is currently available
- **Intuitive UI & navigation**
    - Persistent top-bar + drawer menu for fast navigation between map, weather, configs & sites
    - Context-sensitive info panel in the drawer

---

## Screenshots
TODO: Legg inn screenshots av skjermene her

---

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **DI:** Dagger-Hilt
- **Networking:** Ktor HTTP client
- **Map:** Mapbox Android SDK
- **Turf:** Mapbox SDK Turf
    - Mapbox extension for geospatial tooling. Used in our project for distance & bearing calculations between points, in order to render visuals directly onto terrain.
- **Database:** Room
- **Image Loading:** Coil
- **APIs:**
    - MET Locationforecast
    - MET IsobaricGrib GRIB2 data
    - MET Sunrise/Sunset
- **Build & Dependency Management:**
    - **Gradle** (Kotlin DSL)
    - Dependencies resolved from **Maven Central**
- **Annotation Processing:** KSP
- **Testing Framework:** JUnit
- **Grib2 Parsing:** CDM core and Grib libraries, from NetCDF
    - Frameworks for writing GRIB2 formatted binary files into NetCDF files, structuring data into different types of accessible nD-arrays(arrays indexed by combining n-amount of values). For GRIB2 we get 4D-arrays that are indexed by referencing the 1D-arrays from the same file, holding values for latitude, longitude, isobaric level and time.
- **System UI control:** Accompanist Systemuicontroller
    - Used to control coloring of system components visible in the app, to create a coherent visual while using the app.
- **Dependencies for other libraries:** Guava & Listenablefuture
    - Guava and Listenablefuture are used by several of our other libraries, and as such we needed to install specific versions of them to resolve duplicate paths/dependency conflicts.


---

## Getting Started

Follow these steps to get SOAR up and running on your machine. You can obtain the source either from GitHub or via the ZIP file in Devilry.

### Prerequisites
**Necessary minimums:**
- **Java Development Kit (JDK)**: version 11 or newer
- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **Android SDK**:
    - **Compile SDK:** Platform 35 installed or newer
    - **Minimum SDK:** 26 (Android 8.0 “Oreo”) 
- **Mapbox Access Token & MET API Key:** For this delivery version of the project we've integrated our own tokens and API keys and trust you to not abuse them. For a public version of this app we would include instructions for setting up your own keys, and possibility to register a profile to hold your credentials for the API key and your mapbox token.

**Recommendations:**
- **Mid-to-high-end Android Device:** SOAR performs heavy weather‐data processing and 3D trajectory rendering, so we recommend using a mid-to-high-end Android device for the best experience.

### Installation

#### Option 1: Clone from GitHub
1. Navigate to your preferred directory in terminal
2. Run the commands below:
```bash
git clone https://github.uio.no/IN2000-V25/team-21
cd team-21
```

#### Option 2: Download ZIP from Devilry
1. Log in to Devilry
2. Download the SOAR.zip map for this project.
3. Navigate to the directory you downloaded SOAR.zip to in terminal
4. Unzip the archive:
    1. For Unix:  
    Unzip it with the commands below in bash
    ```bash
    unzip SOAR.zip -d SOAR
    cd SOAR
    ```
    2. For windows: 
    Unzip it with the commands below in PowerShell 
    - change the path if you want it installed somewhere other than C:\SOAR 
    ```cmd
    Expand-Archive SOAR.zip -DestinationPath C:\SOAR
    cd C:\SOAR

    Example custom path:
    Expand-Archive SOAR.zip -DestinationPath C:\JohnDoe\Projects\SOAR
    ```

   
#### Import into Android Studio
1. Launch Android Studio
2. Choose "Open an existing Android Studio project"
3. Navigate to the SOAR/ directory and click OK.
4. Wait for Gradle to sync and download dependencies
5. The app is now ready for use! Connect your chosen Android device (with USB-debugging enabled in developer settings) or choose a simulated device (minimum API 26) to run the app with the Android Studio play button.

---

## Usage

### Find Launch Windows
1. On the **Map** screen, long-press to place a marker.
2. Long press the marker label to save and name the launch site.
3. Click the **Weather** button in the bottom right corner to open the weather screen for your chosen launch site.
4. Swipe horizontally between days.
5. Swipe vertically to see more hourly cards with launch window viability evaluations.
6. Click any hourly card to expand it and see full Locationforecast data
7. For any expanded hourly card, click the "Get Isobaric Data" button to fetch GRIB2 wind and temperature data.
    - This updates the launch window viability evaluation based on the additional data.
9. Use the bottom bar to:
    - Swap active launch site
    - Filter Safe/Caution/Unsafe evaluations & sunrise/sunset windows
    - Switch or create new **Weather Configurations**

### Trajectory Simulator

1. From the Map click the **Trajectory** button.
2. In the pop-up sheet at the bottom of the screen verify that you've chosen the correct launch site.
3. Select (or edit) a rocket configuration
4. Tap **Start Simulation** to plot 3D markers along the flight path.

---

## Navigation & UI
- **Top Bar & Drawer**
    - Logo in top-left opens left-drawer
    - Drawer links: Map, Weather, Configs, Launch Sites
    - Contextual help panel at the bottom of the drawer with information about the current active screen
    - Icon in top right Switches between light and dark mode
- **Map Screen Controls**
    - Bottom-left: Open an interactible menu of all your launch sites → center map on selection
    - Bottom-right: Go to Weather screen for the currently selected launch site
    - Top: Manual coordinates entry field → Places a marker when pressing done
    - Eye-icon: Toggle marker labels on/off
    - Trajectory button: Opens bottom sheet
    - Trajectory bottom sheet:
        - Horizontally scrollable carousel for selecting rocket config for trajectory simulation
        - Edit Rocket Configs: navigates to the rocket config editor
        - Simulate trajectory: Starts a simulation for your chosen rocket config at your current launchsite.
- **Weather Screen Controls**
    - Scroll horizontally to change between days.
    - Scroll vertically to see more hourly cards for the chosen day.
    - Hourly cards: Click to expand with more data
        - Get Isobaric Data: Click on an expanded card to display isobaric data
    - Bottom bar:
        - Launch: Open a display of launchsites. Click to change which site you're displaying the weather for.
        - Filter: Opens filter selection. Choose what data to display and hide.
        - Config: Click a weather config to use it or edit profiles to go to the weather config edit screen.
- **Config Screen Controls**
    - Press Weather Configs or Rocket Configs to go the the respective screen.
    - Press the Green pencil icon on a card to Edit the configuration profile.
    - Press the Red garbage bin icon on a card to delete the configuration profile.
- **Error Screen**
    - Displays custom error descriptions
    - Reload button

---

## Known Issues

---

## Acknowledgments
- MET Norway APIs for weather data
- Mapbox for interactive map
- Portalspace UiO for the task
