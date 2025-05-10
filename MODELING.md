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
* **Navigation & configuration load:** When the user navigates to the Weather screen, the composable immediately asks the ViewModel for the default weather configuration. The ViewModel calls `WeatherConfigRepository.getDefaultWeatherConfig()` and returns the active config back to the UI.
* **Coordinates & forecast fetch:** The screen then collects the current coordinates from the ViewModel and instructs it to load a 120-hour forecast. The ViewModel fetches time-zone–adjusted forecast data from `LocationForecastRepository`, then for each date in the returned series it calls `SunriseRepository.getValidSunTimes()` before emitting a `Success` UI state.
* **Rendering forecast cards:** Once the UI state is `Success`, the screen composes its DailyForecastCards to show per-day summaries and HourlyExpandableCards for each hourly forecast item.
* **Hourly card initial state:** When the user taps to expand a specific hourly card, the UI toggles that card’s expanded state and displays a “Get Isobaric Data” button, allowing the user to trigger loading of upper-air wind data for that time slot.

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
* **Hourly card expansion:** When the user taps an hourly card, the UI toggles that card’s expanded state and immediately shows a “Get Isobaric Data” button if no upper-air data is yet loaded.
* **Isobaric data fetch:** Clicking “Get Isobaric Data” calls `WeatherViewModel.loadIsobaricData(lat, lon, time)`, which delegates to the domain (`getCurrentIsobaricData`). The domain first loads raw GRIB wind data from the `IsobaricRepository`/`IsobaricDataSource`, then fetches surface forecast data from the `LocationForecastRepository`/`LocationForecastDataSource`.
* **Loading & error states:** While the ViewModel is fetching, the card displays a loading spinner; on failure, it shows an error message with a “Retry” button that re-invokes `loadIsobaricData`.
* **Displaying wind data table:** Once data arrives, the UI calls `evaluateConditions(config, forecastItem, isobaricData)` and `calculateWindShear(isobaricData)` on the domain, then passes the resulting parameter states and shear values into `AWTableContents` to render the detailed atmospheric-wind table.

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
* **Config button interactions:** When the user taps the Config button, `WeatherScreen.onConfigClick()` toggles the configuration overlay. If they choose “Edit Configs,” the UI calls `NavController.navigate(WeatherConfigList)`; if they select a specific configuration, `WeatherVM.setActiveWeatherConfig(cfg)` updates the active config in the ViewModel and re-renders the screen.
* **Filter button interactions:** Tapping the Filter button invokes `WeatherScreen.onFilterClick()`, opening the filter overlay. User actions (`onToggleFilter`, `onHoursChanged`, `onStatusToggled`, `onToggleSunFilter`) update the UI’s filter state, and calling `applyFiltersToForecast()` recomputes which daily and hourly cards are shown before re-rendering them.
* **Launch button interactions:** When the Launch button is tapped, `WeatherScreen.onLaunchClick()` displays the launch-sites overlay. Selecting a site calls `WeatherVM.updateCoordinates(site.lat, site.lon)`, which inserts or updates the “Last Visited” site in `LaunchSiteRepository` and then triggers `loadForecast(lat, lon)`. After the forecast data loads, `WeatherScreen.loadIsobaricData()` fetches upper-air wind data and updates the cards with `atmosphericWindStateSuccess`.

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
* **Screen initialization:** When the user opens the Rocket Config List screen, the UI calls `ConfigViewModel.getAllRocketConfigs()`. The ViewModel delegates to `RocketConfigRepository.findAllRocketConfigs()`, which queries the DAO (`SELECT * FROM rocket_config`) and returns the list to the ViewModel, which then emits it back to the UI.
* **Set default configuration:** If the user taps a non-default config, the UI invokes `ConfigViewModel.setDefaultRocketConfig(id)`. The ViewModel calls the repository’s `setDefaultRocketConfig(id)`, which runs two DAO updates—first clearing the old default, then marking the selected config—and finally notifies the ViewModel. The UI then navigates back.
* **Delete configuration:** When the user deletes a config, the UI calls `ConfigViewModel.deleteRocketConfig(cfg)`. The ViewModel instructs the repository to delete it via the DAO (`DELETE FROM rocket_config WHERE id=…`), and upon completion the ViewModel refreshes the list in the UI.
* **Add new configuration:** Tapping the “+” button navigates from the list screen to the Rocket Config Edit screen with no existing parameters, allowing the user to create a brand‐new configuration.

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
* **Screen initialization:** When navigating to the edit screen (with an optional existing `rocketParameters`), the UI subscribes to `ConfigViewModel.rocketUpdateStatus`. The ViewModel immediately emits the current update status (Idle, Loading, etc.) so the UI can show any initial loading or error indicators.
* **Name uniqueness check:** As the user types a configuration name, the UI calls `checkRocketNameAvailability(name)` on the ViewModel. The ViewModel fetches all existing names from `RocketConfigRepository` → DAO (`SELECT name FROM rocket_config`) and returns an availability status to the UI, enabling real-time validation feedback.
* **Saving or updating:** When the user taps “Save,” the UI invokes `ConfigViewModel.saveOrUpdateRocketConfig(rc)`. The ViewModel branches on whether `rc.id` is null (insert) or not (update), then calls the repository’s corresponding DAO operation (`INSERT` or `UPDATE rocket_config`). Once the database operation completes successfully, the ViewModel emits `updateStatus = Success`.
* **Navigation on success:** After `updateStatus` transitions to Success, the edit screen calls `NavController.navigateBack()` to return to the config list, reflecting the newly created or updated configuration.

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
* **Screen initialization:** When the Weather Config List screen is opened, it calls `ConfigViewModel.getAllWeatherConfigs()`. The ViewModel delegates to `WeatherConfigRepository.findAllWeatherConfigs()`, which queries the DAO (`SELECT * FROM weather_config`) and returns the full list to the UI.
* **Set default configuration:** If the user taps a non-default config, the UI invokes `ConfigViewModel.setDefaultWeatherConfig(id)`. The ViewModel tells the repository to clear the old default (`UPDATE weather_config SET is_default=0`) and mark the chosen config as default (`UPDATE weather_config SET is_default=1 WHERE id=…`), then notifies the UI and navigates back.
* **Delete configuration:** When the user deletes a config, the UI calls `ConfigViewModel.deleteWeatherConfig(cfg)`. The ViewModel instructs the repository to remove it via the DAO (`DELETE FROM weather_config WHERE id=…`), and once that completes the ViewModel updates the list in the UI.
* **Add new configuration:** Tapping the “+” button navigates from the list screen to the Weather Config Edit screen with no existing config data, allowing the user to create a brand-new weather configuration.

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
* **Screen initialization:** Upon navigation to the Weather Config Edit screen (with an optional existing config), the UI subscribes to `ConfigViewModel.weatherUpdateStatus`, and the ViewModel immediately emits its current status so the screen can show loading or error indicators as needed.
* **Name uniqueness check:** As the user types the configuration name, the UI calls `checkWeatherNameAvailability(name)` on the ViewModel, which fetches all existing names from `WeatherConfigRepository` → DAO (`SELECT name FROM weather_config ORDER BY name`) and returns an availability status for real-time validation.
* **Saving or updating:** When “Save” is tapped, the UI invokes `ConfigViewModel.saveOrUpdateWeatherConfig(cfg)`. The ViewModel determines whether to insert or update, calls the repository’s corresponding DAO method (`INSERT` or `UPDATE weather_config`), and upon success emits `updateStatus = Success`.
* **Navigation on success:** After the ViewModel signals a successful save, the edit screen calls `NavController.navigateBack()` to return to the list, reflecting the newly created or updated weather configuration.

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

# Class diagrams
### Map Screen
```mermaid
classDiagram
    class MapScreenViewModel {
        <<HiltViewModel>>
        - launchSiteRepository: LaunchSiteRepository
        - rocketConfigRepository: RocketConfigRepository
        - isobaricInterpolator: IsobaricInterpolator
        + uiState: StateFlow&lt;MapScreenUiState&gt;
        + coordinates: StateFlow&lt;Pair&lt;Double, Double&gt;&gt;
        + launchSites: StateFlow&lt;List&lt;LaunchSite&gt;&gt;
        + selectedConfig: StateFlow&lt;RocketConfig?&gt;
        + trajectoryPoints: StateFlow&lt;List&lt;Triple&lt;RealVector, Double, RocketState&gt;&gt;&gt; 
        + startTrajectory: () -> Unit
        + clearTrajectory: () -> Unit
        + selectConfig: (site: RocketConfig) -> Unit
        + updateCoordinates: (lat: Double, lon: Double) -> Unit
        + updateLastVisited: (lat: Double, lon: Double, elevation: Double?) -> Unit
        + updateNewMarker: (lat: Double, lon: Double, elevation: Double?) -> Unit
        + editLaunchSite: (siteId: Int, lat: Double, lon: Double, elevation: Double?, name: String) -> Unit
        + addLaunchSite: (lat: Double, lon: Double, elevation: Double?, name: String) -> Unit
        + geocodeAddress: (address: String) -> Pair&lt;Double, Double&gt;?
        + updateSiteElevation: (siteId: Int, elevation: Double) -> Unit
    }

    class MapScreen {
        + MapScreen(
            viewModel: MapScreenViewModel,
            onNavigateToWeather: (Double, Double) -> Unit
          ) : Unit
    }

    class MapView {
        + MapView(
            center: Pair&lt;Double, Double&gt;,
            newMarker: LaunchSite?,
            newMarkerStatus: Boolean,
            launchSites: List&lt;LaunchSite&gt;,
            mapViewportState: MapViewportState,
            modifier: Modifier,
            showAnnotations: Boolean,
            onMapLongClick: (Point, Double?) -> Unit,
            onMarkerAnnotationClick: (Point, Double?) -> Unit,
            onMarkerAnnotationLongPress: (Point, Double?) -> Unit,
            onLaunchSiteMarkerClick: (LaunchSite) -> Unit,
            onSavedMarkerAnnotationLongPress: (LaunchSite) -> Unit,
            onSiteElevation: (Int, Double) -> Unit,
            trajectoryPoints: List&lt;Triple&lt;RealVector, Double, RocketState&gt;&gt;,
            isAnimating: Boolean,
            onAnimationEnd: () -> Unit
          ) : Unit
    }

    class TrajectoryPopup {
        + TrajectoryPopup(
            show: Boolean,
            lastVisited: LaunchSite?,
            currentSite: LaunchSite?,
            rocketConfigs: List&lt;RocketConfig&gt;,
            selectedConfig: RocketConfig?,
            onSelectConfig: (RocketConfig) -> Unit,
            onClose: () -> Unit,
            onStartTrajectory: () -> Unit,
            onClearTrajectory: () -> Unit,
            onEditConfigs: () -> Unit,
            modifier: Modifier
          ) : Unit
    }

    class MarkerLabel {
        + MarkerLabel(
            name: String,
            lat: String,
            lon: String,
            elevation: String?,
            isLoadingElevation: Boolean,
            onClick: () -> Unit,
            onDoubleClick: () -> Unit,
            onLongPress: () -> Unit,
            fontSize: TextUnit
          ) : Unit
    }

    class SaveLaunchSiteDialog {
        + SaveLaunchSiteDialog(
            launchSiteName: String,
            onNameChange: (String) -> Unit,
            onDismiss: () -> Unit,
            onConfirm: () -> Unit,
            updateStatus: MapScreenViewModel.UpdateStatus
          ) : Unit
    }

    class LaunchSitesButton {
        + LaunchSitesButton(
            modifier: Modifier,
            onClick: () -> Unit
          ) : Unit
    }

    class LaunchSitesMenu {
        + LaunchSitesMenu(
            launchSites: List&lt;LaunchSite&gt;,
            onSiteSelected: (LaunchSite) -> Unit,
            modifier: Modifier
          ) : Unit
    }

    class RocketConfigCarousel {
        + RocketConfigCarousel(
            rocketConfigs: List&lt;RocketConfig&gt;,
            selectedConfig: RocketConfig?,
            onSelectConfig: (RocketConfig) -> Unit,
            modifier: Modifier
          ) : Unit
    }

    class WeatherNavigationButton {
        + WeatherNavigationButton(
            modifier: Modifier,
            latInput: String,
            lonInput: String,
            onNavigate: (Double, Double) -> Unit,
            context: Context
          ) : Unit
    }

    %% Relationships
    MapScreenViewModel <|.. MapScreen        : uses
    MapScreen --> MapView                   : composes
    MapScreen --> TrajectoryPopup           : composes
    MapScreen --> SaveLaunchSiteDialog      : composes
    MapScreen --> LaunchSitesButton         : composes
    LaunchSitesButton --> LaunchSitesMenu   : composes
    TrajectoryPopup --> RocketConfigCarousel: composes
    MapScreen --> WeatherNavigationButton   : composes
    MapView --> MarkerLabel                 : composes
```

### Weather Screen
```mermaid
classDiagram
    class WeatherViewModel {
        <<HiltViewModel>>
        - locationForecastRepository: LocationForecastRepository
        - weatherConfigRepository: WeatherConfigRepository
        - launchSiteRepository: LaunchSiteRepository
        - weatherModel: WeatherModel
        - sunriseRepository: SunriseRepository
        + uiState: StateFlow&lt;WeatherUiState&gt;
        + windState: StateFlow&lt;AtmosphericWindUiState&gt;
        + activeConfig: StateFlow&lt;WeatherConfig?&gt;
        + configList: StateFlow&lt;List&lt;WeatherConfig&gt;&gt;
        + coordinates: StateFlow&lt;Pair&lt;Double, Double&gt;&gt;
        + lastIsobaricCoordinates: StateFlow&lt;Pair&lt;Double, Double&gt;?&gt;
        + isobaricData: StateFlow&lt;Map&lt;Instant, AtmosphericWindUiState&gt;&gt;
        + currentSite: StateFlow&lt;LaunchSite?&gt;
        + launchSites: StateFlow&lt;List&lt;LaunchSite&gt;&gt;
        + clearIsobaricDataForTime: (time: Instant) -> Unit
        + updateCoordinates: (lat: Double, lon: Double) -> Unit
        + setActiveConfig: (config: WeatherConfig) -> Unit
        + loadForecast: (lat: Double, lon: Double, timeSpanInHours: Int) -> Unit
        + loadIsobaricData: (lat: Double, lon: Double, time: Instant) -> Unit
        + getValidSunTimesList: (lat: Double, lon: Double) -> Unit
    }

    class WeatherScreen {
        + WeatherScreen(
            viewModel: WeatherViewModel,
            navController: NavHostController
          ) : Unit
    }

    class ScreenContent {
        + ScreenContent(
            uiState: WeatherViewModel.WeatherUiState,
            coordinates: Pair&lt;Double, Double&gt;,
            weatherConfig: WeatherConfig,
            filterActive: Boolean,
            selectedStatuses: Set&lt;LaunchStatus&gt;,
            currentSite: LaunchSite?,
            viewModel: WeatherViewModel,
            isSunFilterActive: Boolean
          ) : Unit
    }

    class SiteHeader {
        + SiteHeader(
            site: LaunchSite?,
            coordinates: Pair&lt;Double, Double&gt;,
            modifier: Modifier
          ) : Unit
    }

    class DailyForecastCard {
        + DailyForecastCard(
            forecastItems: List&lt;ForecastDataItem&gt;,
            modifier: Modifier
          ) : Unit
    }

    class HourlyExpandableCard {
        + HourlyExpandableCard(
            forecastItem: ForecastDataItem,
            coordinates: Pair&lt;Double, Double&gt;,
            weatherConfig: WeatherConfig,
            modifier: Modifier,
            viewModel: WeatherViewModel
          ) : Unit
    }

    class AtmosphericWindTable {
        + AtmosphericWindTable(
            viewModel: WeatherViewModel,
            coordinates: Pair&lt;Double, Double&gt;,
            time: Instant
          ) : Unit
    }

    class AWTableContents {
        + AWTableContents(
            item: IsobaricData,
            config: WeatherConfig,
            showTime: Boolean
          ) : Unit
    }

    class AWTimeDisplay {
        + AWTimeDisplay(
            time: String,
            style: TextStyle
          ) : Unit
    }

    class WindLayerHeader {
        + WindLayerHeader(
            altitudeText: String,
            windSpeedText: String,
            windDirectionText: String,
            modifier: Modifier,
            style: TextStyle
          ) : Unit
    }

    class WindDataColumn {
        + WindDataColumn(
            isobaricData: IsobaricData,
            config: WeatherConfig,
            windShearColor: Color
          ) : Unit
    }

    class WindLayerRow {
        + WindLayerRow(
            config: WeatherConfig,
            configParameter: ConfigParameter,
            altitude: Double?,
            windSpeed: Double?,
            windDirection: Double?,
            modifier: Modifier,
            style: TextStyle
          ) : Unit
    }
    
    class DefaultWeatherParameters {
        <<object>>
        + instance: WeatherConfig
    }

    %% Relationships
    WeatherViewModel <|.. WeatherScreen            : uses
    WeatherScreen --> ScreenContent               : composes
    ScreenContent --> SiteHeader                  : composes
    ScreenContent --> DailyForecastCard           : composes
    ScreenContent --> HourlyExpandableCard        : composes
    HourlyExpandableCard --> AtmosphericWindTable : composes
    AtmosphericWindTable --> AWTableContents       : composes
    AWTableContents --> AWTimeDisplay              : composes
    AWTableContents --> WindLayerHeader            : composes
    AWTableContents --> WindDataColumn             : composes
    WindDataColumn --> WindLayerRow                : composes
    
    DefaultWeatherParameters ..> WeatherConfig       : provides

```

### Segmented bottom bar
```mermaid
classDiagram
    class SegmentedBottomBar {
        + SegmentedBottomBar(
            onConfigClick: () -> Unit
            onFilterClick: () -> Unit
            onLaunchClick: () -> Unit 
            modifier: Modifier = Modifier
            ) : Unit
    }

    class LaunchSitesMenuOverlay {
        + LaunchSitesMenuOverlay(launchSites: List&lt;LaunchSite&gt;
         onSiteSelected: (LaunchSite) -> Unit
          onDismiss: () -> Unit
           modifier: Modifier = Modifier
           ) : Unit
    }

    class SiteMenuItemList {
        + SiteMenuItemList(launchSites: List&lt;LaunchSite&gt;
        onSelect: (LaunchSite) -> Unit 
        minWidth: Dp 
        maxWidth: Dp
        ) : Unit
    }

    class SiteMenuItem {
        + SiteMenuItem(site: LaunchSite 
        onClick: () -> Unit 
        minWidth: Dp 
        maxWidth: Dp
        ) : Unit
    }

    class WeatherConfigOverlay {
        + WeatherConfigOverlay(configList: List&lt;WeatherConfig&gt;,
        onConfigSelected: (WeatherConfig) -> Unit,
        onNavigateToEditConfigs: () -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
        ) : Unit
    }

    class EditWeatherConfig {
        + EditWeatherConfig(onClick: () -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier
        ) : Unit
    }

    class WeatherConfigItem {
        + WeatherConfigItem(weatherConfig: WeatherConfig,
        onConfigSelected: (WeatherConfig) -> Unit,
        modifier: Modifier = Modifier
        ) : Unit
    }

    class WeatherFilterOverlay {
        + WeatherFilterOverlay(isFilterActive: Boolean,
        onToggleFilter: () -> Unit,
        hoursToShow: Float,
        onHoursChanged: (Float) -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier,
        selectedStatuses: Set&lt;LaunchStatus&gt;,
        onStatusToggled: (LaunchStatus) -> Unit,
        isSunFilterActive: Boolean,
        onToggleSunFilter: () -> Unit
        ) : Unit
    }

    %% Relationships
    SegmentedBottomBar --> LaunchSitesMenuOverlay    : opens
    SegmentedBottomBar --> WeatherFilterOverlay      : opens
    SegmentedBottomBar --> WeatherConfigOverlay      : opens

    LaunchSitesMenuOverlay --> SiteMenuItemList      : composes
    SiteMenuItemList --> SiteMenuItem               : composes

    WeatherConfigOverlay --> EditWeatherConfig      : composes
    WeatherConfigOverlay --> WeatherConfigItem      : composes

    WeatherFilterOverlay ..> LaunchStatusToggleRow  : uses
    WeatherFilterOverlay ..> SunriseFilter          : uses

```

### Configuration classes
```mermaid
classDiagram
    class ConfigViewModel {
        <<HiltViewModel>>
        - weatherRepo: WeatherConfigRepository
        - rocketRepo: RocketConfigRepository
        + weatherConfigs: Flow&lt;List&lt;WeatherConfig&gt;&gt;
        + rocketConfigs: Flow&lt;List&lt;RocketConfig&gt;&gt;
        + weatherNames: StateFlow&lt;List&lt;String&gt;&gt;
        + rocketNames: StateFlow&lt;List&lt;String&gt;&gt;
        + getWeatherConfig(id: Int): Flow&lt;WeatherConfig?&gt;
        + getRocketConfig(id: Int): Flow&lt;RocketConfig?&gt;
        + updateStatus: StateFlow&lt;ConfigViewModel.UpdateStatus&gt;
        + rocketUpdateStatus: StateFlow&lt;ConfigViewModel.UpdateStatus&gt;
        + saveWeatherConfig(cfg: WeatherConfig): Unit
        + updateWeatherConfig(cfg: WeatherConfig): Unit
        + deleteWeatherConfig(cfg: WeatherConfig): Unit
        + checkWeatherNameAvailability(name: String): Unit
        + resetWeatherStatus(): Unit
        + saveRocketConfig(rc: RocketConfig): Unit
        + updateRocketConfig(rc: RocketConfig): Unit
        + deleteRocketConfig(rc: RocketConfig): Unit
        + checkRocketNameAvailability(name: String): Unit
        + resetRocketStatus(): Unit
    }

    class ConfigType {
        <<sealed>>
        - route: String
        - label: String
    }
    class ConfigTypeWeather {
        <<object>>
    }
    class ConfigTypeRocket {
        <<object>>
    }

    class ConfigScreen {
        + ConfigScreen(
            modifier: Modifier = Modifier,
            onWeatherConfigsClick: () -> Unit,
            onRocketConfigsClick: () -> Unit
        ): Unit
    }

    class WeatherConfigListScreen {
        + WeatherConfigListScreen(
            viewModel: ConfigViewModel = hiltViewModel(),
            onEditConfig: (WeatherConfig) -> Unit,
            onAddConfig: () -> Unit,
            onSelectConfig: (WeatherConfig) -> Unit
        ): Unit
    }
    class WeatherConfigListItem {
        + WeatherConfigListItem(
            weatherConfig: WeatherConfig,
            onClick: () -> Unit,
            onEdit: () -> Unit,
            onDelete: () -> Unit
        ): Unit
    }
    class WeatherConfigEditScreen {
        + WeatherConfigEditScreen(
            weatherConfig: WeatherConfig? = null,
            viewModel: ConfigViewModel = hiltViewModel(),
            onNavigateBack: () -> Unit
        ): Unit
    }

    class RocketConfigListScreen {
        + RocketConfigListScreen(
            viewModel: ConfigViewModel = hiltViewModel(),
            onEditRocketConfig: (RocketConfig) -> Unit,
            onAddRocketConfig: () -> Unit,
            onSelectRocketConfig: (RocketConfig) -> Unit
        ): Unit
    }
    class RocketConfigItem {
        + RocketConfigItem(
            rocketConfig: RocketConfig,
            onClick: () -> Unit,
            onEdit: () -> Unit,
            onDelete: () -> Unit
        ): Unit
    }
    class RocketConfigEditScreen {
        + RocketConfigEditScreen(
            rocketParameters: RocketConfig? = null,
            viewModel: ConfigViewModel = hiltViewModel(),
            onNavigateBack: () -> Unit
        ): Unit
    }

    %% Inheritance
    ConfigType <|-- ConfigTypeWeather
    ConfigType <|-- ConfigTypeRocket

    %% Usage
    ConfigViewModel <|.. WeatherConfigListScreen : uses
    ConfigViewModel <|.. WeatherConfigEditScreen : uses
    ConfigViewModel <|.. RocketConfigListScreen  : uses
    ConfigViewModel <|.. RocketConfigEditScreen  : uses

    %% Navigation
    ConfigScreen --> WeatherConfigListScreen    : navigates
    ConfigScreen --> RocketConfigListScreen     : navigates

    %% Composition
    WeatherConfigListScreen --> WeatherConfigListItem : composes
    RocketConfigListScreen  --> RocketConfigItem       : composes
```
## Navigation
```mermaid
classDiagram
    class AppViewModels {
        + maps: MapScreenViewModel
        + weather: WeatherViewModel
        + configs: ConfigViewModel
    }

    class AppScaffold {
        + AppScaffold(darkTheme: Boolean,
        toggleTheme: () -> Unit
        ) : Unit
    }

    class AppDrawer {
        + AppDrawer(navController: NavHostController,
        closeDrawer: () -> Unit
        ) : Unit
    }

    class AppTopBar {
        + AppTopBar(navController: NavHostController,
        currentThemeDark: Boolean,
        onToggleTheme: () -> Unit,
        onOpenDrawer: () -> Unit
        ) : Unit
    }

    class NavigationGraph {
        + NavigationGraph(
            navController: NavHostController,
            innerPadding: PaddingValues,
            mapScreenViewModel: MapScreenViewModel,
            weatherCardViewModel: WeatherViewModel,
            configViewModel: ConfigViewModel
        ) : Unit
    }

    class Screen {
        <<sealed>>
        + route: String
    }
    class Maps          
    class Weather       {createRoute(lat: Double, lon: Double) : String }
    class LaunchSite    
    class Configs      
    class WeatherConfigList 
    class WeatherConfigEdit {+ createRoute(weatherId: Int) : String }
    class RocketConfigList  
    class RocketConfigEdit  {+ createRoute(rocketName: String, rocketId: Int) : String }

    class NavHostController

    %% Composition & usage
    AppScaffold       --> AppDrawer            : composes
    AppScaffold       --> AppTopBar            : composes
    AppScaffold       --> NavigationGraph      : composes
    AppScaffold       --> AppViewModels        : uses

    AppDrawer         --> NavHostController    : uses
    AppDrawer         --> Screen               : uses

    AppTopBar         --> NavHostController    : uses
    AppTopBar         --> Screen               : uses

    NavigationGraph   --> NavHostController    : uses
    AppViewModels   --> MapScreenViewModel   : initializes
    AppViewModels   --> WeatherViewModel     : initializes
    AppViewModels   --> ConfigViewModel      : initializes
    NavigationGraph   --> Screen               : navigates

    %% Screen hierarchy
    Screen <|-- Maps
    Screen <|-- Weather
    Screen <|-- LaunchSite
    Screen <|-- Configs
    Screen <|-- WeatherConfigList
    Screen <|-- WeatherConfigEdit
    Screen <|-- RocketConfigList
    Screen <|-- RocketConfigEdit
```