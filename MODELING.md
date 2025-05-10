> **Disclaimer**  
> The following diagrams are intended to illustrate the conceptual architecture and flow of data within the main components of the app, specifically the `WeatherScreen`, `MapScreen`, and `ConfigScreens`, and their interaction with backend systems.  
> - The diagrams are based on the current design and may not reflect future changes or refactorings.
> - Diagram content is simplified for clarity and may omit certain details such as error handling, concurrency, or edge cases.
# Map Screen
### App launch -> MapScreen
* **App launch & navigation setup:** When the user opens the app, `MainActivity.onCreate()` sets the Compose content to `App()`, which wraps everything in `AppScaffold` (app-wide theming) and then instantiates the `NavigationGraph` with `"maps"` as the start destination—so the `NavHostController` immediately shows `MapScreen` with its ViewModel and navigation callbacks.
* **MapScreen initialization & data load:** As soon as `MapScreen` is composed, it calls `viewModel.loadLaunchSites()`, which asks `LaunchSitesRepository` to fetch all sites via the Room DAO (`queryAllLaunchSites()`), and the resulting list is emitted back to the screen as `uiState = Success(launchSitesList)`.
* **MapView composition:** Once the launch-sites data is available, `MapScreen` composes its `MapView`, passing in the current map center and the loaded `launchSites`, so the map can render markers immediately.

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant MainActivity
    participant App
    participant AppScaffold
    participant NavHost as NavigationGraph
    participant NavController as NavHostController
    participant MapScreen
    participant ViewModel as MapScreenViewModel
    participant LaunchRepo as LaunchSitesRepository
    participant RoomDB as LaunchSiteDatabase

    %% 1) App launch & navigation setup
    User->>MainActivity: launchApp  
    activate MainActivity
    MainActivity->>MainActivity: onCreate() 
    MainActivity-->>MainActivity: setContent { App() }  
    MainActivity->>App: compose(darkTheme,toggleTheme)  
    activate App
    App->>AppScaffold: AppScaffold(darkTheme,toggleTheme)
    activate AppScaffold
    AppScaffold->>NavHost: NavigationGraph(navController,…) 
    activate NavHost
    NavHost->>NavController: NavHost(startDestination=maps)  
    activate NavController
    NavController-->>NavHost: invoke composable(Maps.route)  
    NavHost-->>MapScreen: compose MapScreen(viewModel, onNavigateToWeather)  
    deactivate NavController
    deactivate NavHost
    deactivate AppScaffold
    deactivate App

    %% 2) MapScreen initialization and data load
    activate MapScreen
    MapScreen->>ViewModel: loadLaunchSites()  
    activate ViewModel
    ViewModel->>LaunchRepo: getAllLaunchSites()  
    activate LaunchRepo
    LaunchRepo->>RoomDB: queryAllLaunchSites()  
    RoomDB-->>LaunchRepo: launchSitesList  
    LaunchRepo-->>ViewModel: launchSitesList  
    deactivate LaunchRepo
    ViewModel-->>MapScreen: uiState = Success(launchSitesList)  
    deactivate ViewModel

    %% 3) Compose MapView and request forecast if needed
    MapScreen-->>MapScreen: compose MapView(center, launchSites)  
    deactivate MapScreen
```

### MapScreen - MarkerAnnotation and LaunchSite handling
* **Long-press placeholder creation:** When the user long-presses on the map, `MapView` calls `MapScreen.onMarkerPlaced`, which delegates to the ViewModel’s `onMarkerPlaced(lat, lon)`. The ViewModel first updates the “Last Visited” placeholder (inserting or updating a LaunchSite named “Last Visited” in the database), then does the same for the “New Marker” placeholder, and finally signals the screen that a new marker is ready.
* **Save dialog workflow:** Triggered separately (e.g. via a UI button), the screen shows `SaveLaunchSiteDialog` for the “New Marker.” When the user confirms a name, the dialog passes it back to `MapScreen`, which calls `viewModel.addLaunchSite(name, lat, lon)`. The ViewModel inserts a new `LaunchSite` entity into the repository/DAO/Room layers, then notifies the screen of success and hides the dialog.
* **Double-click annotation handling:** A double-click on any existing marker invokes `MapView` → `MapScreen.onSavedMarkerAnnotationClick(site)`, and the screen calls `viewModel.updateCoordinates(lat, lon)`. The ViewModel updates the “Last Visited” record in the database and returns, after which the screen eases the map camera to center on that site.
* **Launch-sites menu selection:** Tapping the launch-sites FAB shows `LaunchSitesMenu` with all saved sites. When the user selects one, `MapScreen.onSiteSelected(site)` calls `viewModel.updateCoordinates`, which updates the “Last Visited” entry; then the screen dismisses the menu and recenters the map on the chosen site.

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant MapView
    participant MapScreen
    participant SaveDialog as SaveLaunchSiteDialog
    participant LSButton as LaunchSitesButton
    participant LSM as LaunchSitesMenu
    participant VM as MapScreenViewModel
    participant Repo as LaunchSiteRepository
    participant DAO as LaunchSiteDAO
    participant DB as RoomDatabase


    %% 1) Map long-press: placeholder creation only
    User->>MapView: onMapLongClick(lat,lon)  
    MapView->>MapScreen: onMarkerPlaced(lat,lon)  
    MapScreen->>VM: onMarkerPlaced(lat,lon,null)  
    activate VM
    VM->>Repo: updateLastVisited(lat,lon,null)  
    activate Repo
    Repo->>DAO: checkIfSiteExists("Last Visited")
    DAO->>DB: SELECT * FROM LaunchSite WHERE name="Last Visited"
    DB-->>DAO: maybeLaunchSite
    alt exists
        DAO->>DB: UPDATE LaunchSite SET latitude=lat, longitude=lon
    else
        DAO->>DB: INSERT LaunchSite("Last Visited", lat, lon)
    end
    DAO-->>Repo: done
    deactivate Repo

    VM->>Repo: updateNewMarker(lat,lon,null)  
    activate Repo
    Repo->>DAO: checkIfSiteExists("New Marker")
    DAO->>DB: SELECT * FROM LaunchSite WHERE name="New Marker"
    DB-->>DAO: maybeLaunchSite
    alt exists
        DAO->>DB: UPDATE LaunchSite SET latitude=lat, longitude=lon
    else
        DAO->>DB: INSERT LaunchSite("New Marker", lat, lon)
    end
    DAO-->>Repo: done
    Repo-->>VM: done
    deactivate Repo

    VM-->>MapScreen: newMarkerReady=true  
    deactivate VM

    %% 2) Save dialog triggered separately by user
    User->>MapScreen: onSaveDialogRequested()  
    MapScreen->>SaveDialog: showFor("New Marker")  
    SaveDialog-->>User: render dialog

    alt User confirms save
        User->>SaveDialog: onConfirm(siteName)  
        SaveDialog->>MapScreen: onSaveConfirm(siteName)  
        MapScreen->>VM: addLaunchSite(siteName,lat,lon)  
        activate VM
        VM->>Repo: insert(LaunchSite(siteName,lat,lon))  
        activate Repo
        Repo->>DAO: insert LaunchSite(name,lat,lon)
        DAO->>DB: INSERT LaunchSite entity
        DB-->>DAO: OK
        DAO-->>Repo: done
        Repo-->>VM: done
        deactivate Repo
        VM-->>MapScreen: saveSuccess  
        deactivate VM
        MapScreen->>SaveDialog: hide()
    end

    %% 3) Double-click existing marker updates Last Visited and centers map
    User->>MapView: onMarkerDoubleClick(site)  
    MapView->>MapScreen: onSavedMarkerAnnotationClick(site)  
    MapScreen->>VM: updateCoordinates(site.lat,site.lon)  
    activate VM
    VM->>Repo: updateLastVisited(site.lat,site.lon,null)  
    activate Repo
    Repo->>DAO: update LaunchSite("Last Visited")  
    DAO->>DB: UPDATE LaunchSite entity  
    DB-->>DAO: OK  
    DAO-->>Repo: done  
    Repo-->>VM: done  
    deactivate Repo
    VM-->>MapScreen: coordinatesUpdated  
    deactivate VM
    MapScreen->>MapView: easeTo(center=site)

    %% 4) Launch Sites menu selection updates Last Visited
    User->>LSButton: click  
    LSButton->>MapScreen: onLaunchSitesClicked()  
    MapScreen->>MapScreen: toggleLaunchMenu  
    MapScreen->>LSM: show(launchSites)  
    LSM-->>User: render launch sites list

    User->>LSM: selectSite(site)  
    LSM->>MapScreen: onSiteSelected(site)  
    MapScreen->>VM: updateCoordinates(site.lat,site.lon)  
    activate VM
    VM->>Repo: updateLastVisited(site.lat,site.lon,null)  
    activate Repo
    Repo->>DAO: update LaunchSite("Last Visited")  
    DAO->>DB: UPDATE LaunchSite entity  
    DB-->>DAO: OK  
    DAO-->>Repo: done  
    Repo-->>VM: done  
    deactivate Repo
    VM-->>MapScreen: coordinatesUpdated  
    deactivate VM
    MapScreen->>MapView: easeTo(center=site)
```

### MapScreen - MapView and Location selection and Trajectory Loading
* **Initialization & launch‐sites load:** When `MapScreen` is composed, it immediately calls `viewModel.loadLaunchSites()`, which invokes `LaunchSitesRepository.getAllLaunchSites()` to query the Room `LaunchSiteDao`. Once the list returns, the ViewModel emits `uiState = Success(launchSitesList)`, allowing the UI to render markers for saved sites.
* **MapView setup:** After launch‐site data is available, `MapScreen` composes `MapView(center, launchSites)`. `MapView` then loads the Mapbox style (satellite with terrain and sky) via `mapboxMap.loadStyle(...)` before rendering the map.
* **Trajectory options popup:** Tapping the “Trajectory” FAB triggers `showTrajectoryPopup()`. Within this overlay, users can select or edit rocket configurations and clear any existing trajectory; these UI actions dispatch to the ViewModel (`setSelectedConfig`, `clearTrajectory`) or navigate to the config list.
* **Trajectory calculation & data fetch:** When “Start Trajectory” is tapped, the ViewModel fetches the default rocket config, then calls `TrajectoryCalculator.calculateTrajectory()`. The calculator first fetches the full GRIB map (once) and then makes an initial grid of 16 forecast calls around the launch site; during simulation, it interpolates wind using the GRIB map and, whenever a quadrant boundary is crossed, fetches four additional forecast points from the `LocationForecastRepository`.
* **Render & animate trajectory:** After `trajectoryPoints` return, `MapScreen` passes them to `MapView.updateTrajectory()`, which loops over each point, adding a GeoJSON source and a 3D model layer at the correct altitude. Finally, `MapView` calls `mapboxMap.animateCameraAlong(trajectoryPoints)`, smoothly flying the camera along the path until completion.

```mermaid
sequenceDiagram
    autonumber
    participant User
    participant MapScreen
    participant MapView
    participant Mapbox as MapboxMap
    participant VM as MapScreenViewModel
    participant NavCtrl as NavController
    participant Domain as TrajectoryCalculator
    participant LaunchRepo as LaunchSiteRepository
    participant RocketRepo as RocketConfigRepository
    participant LocRepo as LocationForecastRepository
    participant GRIBRepo as IsobaricRepository
    participant ForecastDS as LocationForecastDataSource
    participant IsobaricDS as IsobaricDataSource
    participant RoomDB as LaunchSiteDatabase

    %% 1) Initialization: load launch sites
    User->>MapScreen: onCreate/compose  
    activate MapScreen
    MapScreen->>VM: loadLaunchSites()  
    activate VM
    VM->>LaunchRepo: getAllLaunchSites()  
    LaunchRepo->>RoomDB: queryAllLaunchSites()  
    RoomDB-->>LaunchRepo: launchSitesList  
    LaunchRepo-->>VM: launchSitesList  
    VM-->>MapScreen: uiState(launchSites)  
    deactivate VM

    %% 2) MapView setup
    MapScreen->>MapView: initialize(center, launchSites)  
    activate MapView
    MapView->>Mapbox: loadStyle(styleUrl + terrain + sky)  
    Mapbox-->>MapView: styleLoaded  
    deactivate MapView

    %% 3) Trajectory popup flow
    User->>MapScreen: clickTrajectoryFAB()  
    MapScreen->>MapScreen: showTrajectoryPopup  

    alt select rocket config
        User->>MapScreen: onSelectConfig(cfg)  
        MapScreen->>VM: setSelectedConfig(cfg)  
        activate VM
        VM-->>MapScreen: selectedConfig  
        deactivate VM
    end
    alt edit configs
        User->>MapScreen: onEditConfigs()  
        MapScreen->>NavCtrl: navigate(RocketConfigList)  
        activate NavCtrl
        NavCtrl-->>User: showConfigListScreen  
        deactivate NavCtrl
    end
    alt clear trajectory
        User->>MapScreen: onClearTrajectory()  
        MapScreen->>VM: clearTrajectory()  
        activate VM
        VM-->>MapScreen: trajectoryCleared  
        deactivate VM
        MapScreen->>MapView: clearTrajectoryLayers()  
    end

    %% 4) User starts trajectory
    User->>MapScreen: onStartTrajectory()  
    MapScreen->>VM: startTrajectory(initialPosition)  
    activate VM
    VM->>RocketRepo: getDefaultRocketConfig()  
    RocketRepo-->>VM: config  

    VM->>Domain: calculateTrajectory(initialPosition, config)  
    activate Domain

    %% 4a) Pre-fetch full GRIB map once
    Domain->>GRIBRepo: fetchAllIsobaricGribData(timeRange)  
    activate GRIBRepo
    GRIBRepo->>IsobaricDS: fetchIsobaricGribData(timeRange)  
    IsobaricDS-->>GRIBRepo: ByteArray  
    GRIBRepo-->>Domain: gribDataMap  
    deactivate GRIBRepo

    %% 4b) Initial forecast grid around launch: 16 calls
    loop initial grid (16 points)
        Domain->>LocRepo: getForecastData(lat₀+dx, lon₀+dy, startTime)  
        activate LocRepo
        LocRepo->>ForecastDS: fetchForecastData(lat₀+dx, lon₀+dy)  
        ForecastDS-->>LocRepo: ForecastDataResponse  
        LocRepo-->>Domain: forecastData  
        deactivate LocRepo
    end

    %% 4c) Simulation steps with quadrant‐based extra calls
    loop simulation steps
        Domain->>Domain: interpolateWind(gribDataMap, position)
        alt if quadrant boundary crossed
            loop quadrant forecasts (4 points)
                Domain->>LocRepo: getForecastData(lat,lon,currentTime)  
                activate LocRepo
                LocRepo->>ForecastDS: fetchForecastData(lat,lon)  
                ForecastDS-->>LocRepo: ForecastDataResponse  
                LocRepo-->>Domain: forecastData  
                deactivate LocRepo
            end
        end
    end

    Domain-->>VM: trajectoryPoints  
    deactivate Domain
    VM-->>MapScreen: trajectoryPoints  
    deactivate VM

    %% 5) Render trajectory
    MapScreen->>MapView: updateTrajectory(trajectoryPoints)  
    activate MapView
    loop each point in trajectoryPoints
        MapView->>Mapbox: addSource(id, geoJson(point))  
        MapView->>Mapbox: addLayer(id, modelLayer with translationZ)  
    end
    deactivate MapView

    %% 6) Animate camera
    MapView->>Mapbox: animateCameraAlong(trajectoryPoints)  
    Mapbox-->>MapView: animationComplete  
```

# Weather Screen
### Weather Screen Navigation and initialization
```mermaid
sequenceDiagram
    autonumber
    participant User
    participant WeatherUI as WeatherScreen
    participant WeatherVM as WeatherViewModel
    participant ConfigRepo as WeatherConfigRepository
    participant LocRepo as LocationForecastRepository
    participant ForecastDS as LocationForecastDataSource
    participant SunriseRepo as SunriseRepository

    %% Initial navigation & config load
    User->>WeatherUI: navigateTo("weather/...")  
    activate WeatherUI
    WeatherUI->>WeatherVM: collectDefaultWeatherConfig  
    activate WeatherVM
    WeatherVM->>ConfigRepo: getDefaultWeatherConfig()  
    activate ConfigRepo
    ConfigRepo-->>WeatherVM: defaultWeatherConfig  
    deactivate ConfigRepo
    WeatherVM-->>WeatherUI: activeWeatherConfig  
    deactivate WeatherVM

    %% Coordinates & forecast load
    WeatherUI->>WeatherVM: collectCoordinates  
    WeatherVM-->>WeatherUI: coordinates(lat,lon)  
    WeatherUI->>WeatherVM: loadForecast(lat,lon)  
    activate WeatherVM
    WeatherVM->>LocRepo: getTimeZoneAdjustedForecast(lat,lon,120h)  
    activate LocRepo
    LocRepo->>ForecastDS: fetchForecastData(lat,lon)  
    ForecastDS-->>LocRepo: ForecastDataResponse  
    LocRepo-->>WeatherVM: forecastTimeSeries  
    deactivate LocRepo
    loop per date in forecastTimeSeries
        WeatherVM->>SunriseRepo: getValidSunTimes(lat,lon,date)  
        activate SunriseRepo
        SunriseRepo-->>WeatherVM: validSunTimes  
        deactivate SunriseRepo
    end
    WeatherVM-->>WeatherUI: uiState = Success  
    deactivate WeatherVM

    %% Render forecast cards
    WeatherUI->>WeatherUI: renderDailyForecastCards  
    WeatherUI->>WeatherUI: renderHourlyExpandableCards  

    %% Hourly card initial state
    User->>WeatherUI: clickExpandHourlyCard(time)  
    WeatherUI->>WeatherUI: toggleHourlyCardExpanded(time)  
    WeatherUI->>WeatherUI: showGetIsobaricDataButton(time) 
```

### Weather Screen - Hourly Card and Isobaric Data rendering
```mermaid
sequenceDiagram
    autonumber
    participant User
    participant WeatherUI as WeatherScreen
    participant WeatherVM as WeatherViewModel
    participant Domain
    participant GRIBRepo as IsobaricRepository
    participant IsobaricDS as IsobaricDataSource
    participant ForecastDS as LocationForecastDataSource

    User->>WeatherUI: onHourlyCardClick(time)  
    WeatherUI->>WeatherUI: setHourlyCardExpanded(time,true)  

    alt no isobaric data for time
        WeatherUI->>WeatherUI: showGetIsobaricDataButton(time)  
        User->>WeatherUI: onGetIsobaricDataClick(time)  
        WeatherUI->>WeatherVM: loadIsobaricData(lat,lon,time)  
        activate WeatherVM
        WeatherVM->>Domain: getCurrentIsobaricData(lat,lon,time)  
        activate Domain
        Domain->>GRIBRepo: getIsobaricGribData(time)  
        activate GRIBRepo
        GRIBRepo->>IsobaricDS: fetchIsobaricGribData(uri)  
        IsobaricDS-->>GRIBRepo: ByteArray  
        GRIBRepo-->>Domain: GribDataMap  
        deactivate GRIBRepo
        Domain->>ForecastDS: fetchForecastData(lat,lon)  
        ForecastDS-->>Domain: ForecastDataResponse  
        Domain-->>WeatherVM: isobaricDataResult  
        deactivate Domain
        WeatherVM-->>WeatherUI: atmosphericWindStateSuccess(time,data)  
        deactivate WeatherVM

    else loading
        WeatherUI->>WeatherUI: showIsobaricLoading(time)  
    else error
        WeatherUI->>WeatherUI: showIsobaricError(time)  
    end

    %% Display wind data table
    WeatherUI->>WeatherUI: renderAtmosphericWindTable(time)  
    WeatherUI->>Domain: evaluateConditions(config, forecastItem, isobaricData)  
    Domain-->>WeatherUI: parameterStatesList  
    WeatherUI->>Domain: calculateWindShear(isobaricData)  
    Domain-->>WeatherUI: shearValues  
    WeatherUI->>WeatherUI: renderAWTableContents(parameterStatesList, shearValues)  
```

### Segmented Bottom Bar interactions
```mermaid
    sequenceDiagram
    autonumber
    participant User
    participant BottomBar as SegmentedBottomBar
    participant WeatherUI as WeatherScreen
    participant WeatherVM as WeatherViewModel
    participant NavCtrl as NavController
    participant LaunchRepo as LaunchSiteRepository
    participant RoomDB as LaunchSiteDatabase
    participant LocRepo as LocationForecastRepository
    participant ForecastDS as LocationForecastDataSource
    participant SunriseRepo as SunriseRepository
    participant Domain
    participant GRIBRepo as IsobaricRepository
    participant IsobaricDS as IsobaricDataSource

    %% Segmented Bottom Bar interactions (Config, Filter, then Launch)
    alt Config button
        User->>BottomBar: click Config  
        BottomBar->>WeatherUI: onConfigClick()  
        WeatherUI->>WeatherUI: toggleConfigOverlay  
        alt navigate to WeatherConfigList
            WeatherUI->>NavCtrl: navigate(WeatherConfigList)  
            activate NavCtrl
            NavCtrl-->>User: show WeatherConfigListScreen  
            deactivate NavCtrl
        else select WeatherConfig
            WeatherUI->>WeatherVM: setActiveWeatherConfig(config)  
            activate WeatherVM
            WeatherVM-->>WeatherUI: activeWeatherConfigUpdated  
            deactivate WeatherVM
        end
    end

    alt Filter button
        User->>BottomBar: click Filter  
        BottomBar->>WeatherUI: onFilterClick()  
        WeatherUI->>WeatherUI: toggleFilterOverlay  

        %% User adjusts filters
        User->>WeatherUI: onToggleFilter()  
        WeatherUI->>WeatherUI: setFilterActive  
        User->>WeatherUI: onHoursChanged(hours)  
        WeatherUI->>WeatherUI: setHoursToShow(hours)  
        User->>WeatherUI: onStatusToggled(status)  
        WeatherUI->>WeatherUI: updateSelectedStatuses(status)  
        User->>WeatherUI: onToggleSunFilter()  
        WeatherUI->>WeatherUI: setSunFilterActive  

        %% Apply and re-render
        WeatherUI->>WeatherUI: applyFiltersToForecast()  
        WeatherUI->>WeatherUI: renderDailyForecast & renderHourlyForecast  
    end

    alt Launch button
        User->>BottomBar: click Launch  
        BottomBar->>WeatherUI: onLaunchClick()  
        WeatherUI->>WeatherUI: toggleLaunchOverlay  
        alt User selects Launch Site
            User->>WeatherUI: selectLaunchSite(site)  
            WeatherUI->>WeatherVM: updateCoordinates(site.lat,site.lon)  
            activate WeatherVM
            WeatherVM->>LaunchRepo: insertOrUpdateLastVisitedSite(site)  
            activate LaunchRepo
            LaunchRepo->>RoomDB: queryAllLaunchSites()  
            RoomDB-->>LaunchRepo: launchSitesList  
            LaunchRepo-->>WeatherVM: done  
            deactivate LaunchRepo
            WeatherVM-->>WeatherUI: coordinatesUpdated  

            WeatherUI->>WeatherVM: loadForecast(site.lat,site.lon)  
            activate WeatherVM
            WeatherVM->>LocRepo: getTimeZoneAdjustedForecast(site.lat,site.lon,120h)  
            activate LocRepo
            LocRepo->>ForecastDS: fetchForecastData(site.lat,site.lon)  
            ForecastDS-->>LocRepo: ForecastDataResponse  
            LocRepo-->>WeatherVM: forecastTimeSeries  
            deactivate LocRepo
            loop per date in forecastTimeSeries
                WeatherVM->>SunriseRepo: getValidSunTimes(site.lat,site.lon,date)  
                activate SunriseRepo
                SunriseRepo-->>WeatherVM: validSunTimes  
                deactivate SunriseRepo
            end
            WeatherVM-->>WeatherUI: uiState = Success  

            WeatherUI->>WeatherVM: loadIsobaricData(site.lat,site.lon,now)  
            activate WeatherVM
            WeatherVM->>Domain: getCurrentIsobaricData(site.lat,site.lon,now)  
            activate Domain
            Domain->>GRIBRepo: getIsobaricGribData(now)  
            activate GRIBRepo
            GRIBRepo->>IsobaricDS: fetchIsobaricGribData(uri)  
            IsobaricDS-->>GRIBRepo: ByteArray  
            GRIBRepo-->>Domain: GribDataMap  
            deactivate GRIBRepo
            Domain->>ForecastDS: fetchForecastData(site.lat,site.lon)  
            ForecastDS-->>Domain: ForecastDataResponse  
            Domain-->>WeatherVM: isobaricDataResult  
            deactivate Domain
            WeatherVM-->>WeatherUI: atmosphericWindStateSuccess(now,data)  
            deactivate WeatherVM
        end
    end
```
# Rocket Config
### Rocket Config List Screen
```mermaid
sequenceDiagram
    autonumber
    participant User
    participant ConfigListUI as RocketConfigListScreen
    participant VM as ConfigViewModel
    participant Repo as RocketConfigRepository
    participant DAO as RocketConfigDao
    participant DB as RoomDatabase
    participant NavCtrl as NavHostController

    %% 1) Screen initialization: load all configs
    User->>ConfigListUI: navigateTo(RocketConfigList)  
    activate ConfigListUI
    ConfigListUI->>VM: collect rocketConfigs  
    activate VM
    VM->>Repo: getAllRocketConfigs()  
    activate Repo
    Repo->>DAO: findAllRocketConfigs()  
    activate DAO
    DAO->>DB: SELECT * FROM rocket_config  
    DB-->>DAO: list<RocketConfig>  
    DAO-->>Repo: list<RocketConfig>  
    deactivate DAO
    Repo-->>VM: list<RocketConfig>  
    deactivate Repo
    VM-->>ConfigListUI: rocketConfigs  
    deactivate VM
    deactivate ConfigListUI

    %% 2) User selects a config to make default
    alt User taps a non-default config
        User->>ConfigListUI: selectConfig(cfg)  
        ConfigListUI->>VM: setDefaultRocketConfig(cfg.id)  
        activate VM
        VM->>Repo: setDefaultRocketConfig(cfg.id)  
        activate Repo
        Repo->>DAO: setDefaultRocketConfig(cfg.id)  
        activate DAO
        DAO->>DB: UPDATE rocket_config SET is_default=0  
        DB-->>DAO: OK  
        DAO->>DB: UPDATE rocket_config SET is_default=1 WHERE id=cfg.id  
        DB-->>DAO: OK  
        DAO-->>Repo: done  
        deactivate DAO
        Repo-->>VM: done  
        deactivate Repo
        VM-->>ConfigListUI: defaultConfigUpdated  
        ConfigListUI->>NavCtrl: navigateBack()
        deactivate VM
    end

    %% 3) User deletes a config
    alt User taps delete on cfg
        User->>ConfigListUI: deleteConfig(cfg)  
        ConfigListUI->>VM: deleteRocketConfig(cfg)  
        activate VM
        VM->>Repo: deleteRocketConfig(cfg)  
        activate Repo
        Repo->>DAO: deleteRocketConfig(cfg)  
        activate DAO
        DAO->>DB: DELETE FROM rocket_config WHERE id=cfg.id  
        DB-->>DAO: OK  
        DAO->>Repo: done  
        deactivate DAO
        Repo-->>VM: done  
        deactivate Repo
        VM-->>ConfigListUI: rocketConfigsUpdated  
        deactivate VM
    end

    %% 4) User adds a new config
    alt User taps “+”
        User->>ConfigListUI: clickAdd  
        ConfigListUI->>NavCtrl: navigate(RocketConfigEdit, rocketParameters=null)  
    end
```

### Rocket Config Edit Screen
```mermaid
sequenceDiagram
    autonumber
    participant User
    participant EditUI as RocketConfigEditScreen
    participant VM as ConfigViewModel
    participant Repo as RocketConfigRepository
    participant DAO as RocketConfigDao
    participant DB as RoomDatabase
    participant NavCtrl as NavHostController

    %% 1) Screen init: editing or creating
    User->>EditUI: navigateTo(RocketConfigEdit[rocketParameters?])  
    activate EditUI
    EditUI->>VM: collect rocketUpdateStatus  
    activate VM
    VM-->>EditUI: updateStatus  
    deactivate VM
    deactivate EditUI

    %% 2) Name uniqueness check
    loop as user types name
        EditUI->>VM: checkRocketNameAvailability(name)  
        activate VM
        VM->>Repo: getAllRocketConfigNames()  
        activate Repo
        Repo->>DAO: findAllRocketConfigNames()  
        activate DAO
        DAO->>DB: SELECT name FROM rocket_config  
        DB-->>DAO: list<String>  
        DAO-->>Repo: list<String>  
        deactivate DAO
        Repo-->>VM: list<String>  
        deactivate Repo
        VM-->>EditUI: availabilityStatus  
        deactivate VM
    end

    %% 3) User taps “Save Rocket Configuration”
    User->>EditUI: clickSave  
    EditUI->>VM: saveOrUpdateRocketConfig(rc)  
    activate VM
    alt creating new
        VM->>Repo: insertRocketConfig(rc)  
    else updating existing
        VM->>Repo: updateRocketConfig(rc)  
    end
    activate Repo
    Repo->>DAO: insertRocketConfig(rc) or updateRocketConfig(rc)  
    activate DAO
    DAO->>DB: INSERT or UPDATE rocket_config  
    DB-->>DAO: OK  
    DAO-->>Repo: done  
    deactivate DAO
    Repo-->>VM: done  
    deactivate Repo
    VM-->>EditUI: updateStatus = Success  
    deactivate VM

    %% 4) Navigate back on success
    EditUI->>NavCtrl: navigateBack()  
```

# Weather Config
### Weather Config List Screen
```mermaid
sequenceDiagram
    autonumber
    participant User
    participant ListUI as WeatherConfigListScreen
    participant VM as ConfigViewModel
    participant Repo as WeatherConfigRepository
    participant DAO as WeatherConfigDao
    participant DB as RoomDatabase
    participant NavCtrl as NavController

    %% 1) Screen initialization: load all weather configs
    User->>ListUI: navigateTo(WeatherConfigList)  
    activate ListUI
    ListUI->>VM: collect weatherConfigs  
    activate VM
    VM->>Repo: getAllWeatherConfigs()  
    activate Repo
    Repo->>DAO: findAllWeatherConfigs()
    activate DAO
    DAO->>DB: SELECT * FROM weather_config
    DB-->>DAO: list<WeatherConfig>  
    DAO-->>Repo: list<WeatherConfig>  
    deactivate DAO
    Repo-->>VM: list<WeatherConfig>  
    deactivate Repo
    VM-->>ListUI: weatherConfigs  
    deactivate VM
    deactivate ListUI

    %% 2) User selects a config to make default
    alt User taps a non-default config
        User->>ListUI: selectConfig(cfg)  
        ListUI->>VM: setDefaultWeatherConfig(cfg.id)  
        activate VM
        VM->>Repo: setDefaultWeatherConfig(cfg.id)  
        activate Repo
        Repo->>DAO: findDefaultWeatherConfig() + setDefaultRocketConfig implementation 
        activate DAO
        DAO->>DB: UPDATE weather_config SET is_default=0 
        DB-->>DAO: OK  
        DAO->>DB: UPDATE weather_config SET is_default=1 WHERE id=cfg.id 
        DB-->>DAO: OK  
        DAO-->>Repo: done  
        deactivate DAO
        Repo-->>VM: done  
        deactivate Repo
        VM-->>ListUI: defaultConfigUpdated  
        ListUI->>NavCtrl: navigateBack()  
        deactivate VM
    end

    %% 3) User deletes a config
    alt User taps delete on cfg
        User->>ListUI: deleteConfig(cfg)  
        ListUI->>VM: deleteWeatherConfig(cfg)  
        activate VM
        VM->>Repo: deleteWeatherConfig(cfg)  
        activate Repo
        Repo->>DAO: deleteWeatherConfig(cfg)  
        activate DAO
        DAO->>DB: DELETE FROM 
        DB-->>DAO: OK  
        DAO->>Repo: done  
        deactivate DAO
        Repo->>VM: done  
        deactivate Repo
        VM-->>ListUI: weatherConfigsUpdated  
        deactivate VM
    end

    %% 4) User adds a new config
    alt User taps “+”
        User->>ListUI: clickAdd  
        ListUI->>NavCtrl: navigate(WeatherConfigEdit, weatherConfig=null)  
    end
```

### Weather Config Edit Screen
```mermaid
sequenceDiagram
    autonumber
    participant User
    participant EditUI as WeatherConfigEditScreen
    participant VM as ConfigViewModel
    participant Repo as WeatherConfigRepository
    participant DAO as WeatherConfigDao
    participant DB as RoomDatabase
    participant NavCtrl as NavController

    %% 1) Screen init: editing or creating
    User->>EditUI: navigateTo(WeatherConfigEdit[config?])  
    activate EditUI
    EditUI->>VM: collect weatherUpdateStatus  
    activate VM
    VM-->>EditUI: updateStatus  
    deactivate VM

    %% 2) Name uniqueness check
    loop as user types name
        EditUI->>VM: checkWeatherNameAvailability(name)  
        activate VM
        VM->>Repo: getAllWeatherConfigNames()  
        activate Repo
        Repo->>DAO: findAllWeatherConfigNames() :contentReference[oaicite:11]{index=11}:contentReference[oaicite:12]{index=12}  
        activate DAO
        DAO->>DB: SELECT name FROM weather_config ORDER BY name :contentReference[oaicite:13]{index=13}:contentReference[oaicite:14]{index=14}  
        DB-->>DAO: list<String>  
        DAO-->>Repo: list<String>  
        deactivate DAO
        Repo-->>VM: list<String>  
        deactivate Repo
        VM-->>EditUI: availabilityStatus  
        deactivate VM
    end

    %% 3) User taps “Save Configuration”
    User->>EditUI: clickSave  
    EditUI->>VM: saveOrUpdateWeatherConfig(cfg)  
    activate VM
    alt creating new
        VM->>Repo: insertWeatherConfig(cfg)  
    else updating existing
        VM->>Repo: updateWeatherConfig(cfg)  
    end
    activate Repo
    Repo->>DAO: insertWeatherConfig(cfg) or updateWeatherConfig(cfg)  
    activate DAO
    DAO->>DB: INSERT or UPDATE weather_config :contentReference[oaicite:15]{index=15}:contentReference[oaicite:16]{index=16}  
    DB-->>DAO: OK  
    DAO->>Repo: done  
    deactivate DAO
    Repo-->>VM: done  
    deactivate Repo
    VM-->>EditUI: updateStatus = Success  
    deactivate VM

    %% 4) Navigate back on success
    EditUI->>NavCtrl: navigateBack()  
```