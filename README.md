**English** · [Español](README.es.md)

Logroño Bus App
==============

![kotlin-version](https://img.shields.io/badge/kotlin-2.4.21-blue?logo=kotlin)
![compose-multiplatform](https://img.shields.io/badge/compose%20multiplatform-1.13.0--beta01-blue)
![license](https://img.shields.io/badge/license-Apache%202.0-blue)
![build](https://github.com/Adrc95/logrono-bus/actions/workflows/build.yml/badge.svg)
[![web demo](https://img.shields.io/badge/demo-adrc95.github.io%2Flogrono--bus-ff2d55)](https://adrc95.github.io/logrono-bus/)

Logroño Bus helps you plan journeys on Logroño’s urban bus network. Check upcoming departures at
saved stops, find nearby stops, track buses on a map and search the full stop list. You can also use
your camera to scan the code displayed at a bus stop.

> [!IMPORTANT]
> This is a personal, **unofficial** project. It is not affiliated with Logroño City Council or the
> company that operates the buses. The app uses the council’s public service for timetable data (GTFS)
> and real-time updates (GTFS-Realtime).

The UI is written once with Compose Multiplatform and runs on Android, iOS, desktop and web.

If you want to see how the code is organised, there are two documents: the
[modularization guide](docs/ModularizationLearningJourney.md) and the
[architecture guide](docs/ArchitectureLearningJourney.md). The architecture guide is also available in
[Spanish](docs/ArchitectureLearningJourney.es.md).

# Features

- **Saved stops.** See upcoming departures for the stops you follow, including line colours and delays.
- **Nearby stops.** Find the closest stops and check how long it takes to walk to them.
- **Live tracking.** See a bus on the map, follow its route and check expected arrival times at each
  stop. Tracking ends when the bus reaches its terminus.
- **Stop search.** Search by stop name and view its departures.
- **Stop scanner.** Scan the code displayed at a stop using your camera.
- **Buses, lines and fares.** Check which buses are running, explore the network’s lines and view fares.

## Screenshots

**Android (Jetpack Compose)**

<table>
  <tr>
    <td><img src="screenshots/android-mystops.png" width="200" height="407" alt="Saved stops with their next departures"/></td>
    <td><img src="screenshots/android-nearby.png" width="200" height="407" alt="Nearby stops"/></td>
    <td><img src="screenshots/android-tracking.png" width="200" height="407" alt="Live bus tracking"/></td>
    <td><img src="screenshots/android-search.png" width="200" height="407" alt="Stop search"/></td>
  </tr>
  <tr>
    <td align="center">Saved stops</td>
    <td align="center">Near you</td>
    <td align="center">Tracking</td>
    <td align="center">Search</td>
  </tr>
</table>

**iOS (Compose Multiplatform)**

<table>
  <tr>
    <td><img src="screenshots/ios-mystops.png" width="200" height="435" alt="Saved stops with their next departures"/></td>
    <td><img src="screenshots/ios-nearby.png" width="200" height="435" alt="Nearby stops"/></td>
    <td><img src="screenshots/ios-tracking.png" width="200" height="435" alt="Live bus tracking"/></td>
    <td><img src="screenshots/ios-search.png" width="200" height="435" alt="Stop search"/></td>
  </tr>
  <tr>
    <td align="center">Saved stops</td>
    <td align="center">Near you</td>
    <td align="center">Tracking</td>
    <td align="center">Search</td>
  </tr>
</table>

The same UI also runs on desktop and web.

# Development environment

Build the project with the included Gradle wrapper. Install these tools first:

- **JDK 17** — the Java toolchain used by all modules.
- **Android Studio** and **Android SDK 37** — needed for Android and for the desktop and web targets.
- **Xcode** — needed to build and run the iOS app in [`app/iosApp`](./app/iosApp).

Open the repository root in your IDE and let it import the project. The versions that matter are in
[`gradle/libs.versions.toml`](./gradle/libs.versions.toml).

# Libraries

| Library | What it is for |
|---|---|
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | The code shared by Android, iOS, desktop and web |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | The UI, written once |
| [Material 3](https://m3.material.io/) | The visual system, adaptive variants included |
| [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | The view model and the state the screen observes |
| [Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity) | The Android entry point |
| [Koin](https://insert-koin.io/) | Dependency injection resolved at compile time |
| [Ktor](https://ktor.io/) | HTTP client, with an engine per platform |
| [Ktorfit](https://github.com/Foso/Ktorfit) | Endpoints declared as interfaces |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | JSON and navigation keys |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Concurrency and flows |
| [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) | Dates and times |
| [kotlinx-collections-immutable](https://github.com/Kotlin/kotlinx.collections.immutable) | Immutable collections in the state |
| [kotlinx-io](https://github.com/Kotlin/kotlinx-io) | Bytes and files, underneath DataStore |
| [kotlinx-browser](https://github.com/Kotlin/kotlinx-browser) | Browser APIs on the web target |
| [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) | The back stack, which gets saved |
| [NavigationEvent](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture) | The back gesture |
| [Room](https://developer.android.com/kotlin/multiplatform/room) | The local database, with the native driver or the browser one |
| [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | Preferences |
| [MapLibre Compose](https://github.com/maplibre/maplibre-compose) | The map on Android, iOS and web |
| [JetLime](https://github.com/pushpalroy/jetlime) | The tracking timeline |
| [CameraX](https://developer.android.com/media/camera/camerax) | The scanner camera, on Android |
| [ML Kit](https://developers.google.com/ml-kit/vision/text-recognition/v2) | Reading the code on the stop sign on Android |
| [Vision](https://developer.apple.com/documentation/vision) | Reading the code on the stop sign on iOS |
| [Grant](https://github.com/brewkits/Grant) | Camera and location permissions |
| [Play Services Location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) | Location on Android |
| [CoreLocation](https://developer.apple.com/documentation/corelocation) | Location on iOS |
| [Napier](https://github.com/AAkira/Napier) | Logs, with each platform's logger |
| [kotlin-test](https://kotlinlang.org/api/core/kotlin-test/) and [coroutines-test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) | The test framework |
| [Turbine](https://github.com/cashapp/turbine) | Checking flows in tests |

# Architecture

The code is organised into model, domain, data and presentation layers. Dependencies flow in one
direction, which keeps each layer focused on its role. The shared Compose Multiplatform UI is split
between the design-system module and the shared UI components module.

The [architecture guide](docs/ArchitectureLearningJourney.md) explains each layer and the boundaries
it follows.

# Modularization

Gradle modules are grouped into three types: `core` libraries, feature modules and applications. The
shared UI host and platform application projects are grouped in `app/`; `androidApp`, `webApp`,
and `desktopApp` are Gradle modules, while `iosApp` remains a native Xcode project. Convention
plugins keep plugin, target and dependency setup consistent across modules. Each module’s README
includes its dependency graph.

See the [modularization guide](docs/ModularizationLearningJourney.md) for details.

# Build

| Platform | Module | How to run it |
|---|---|---|
| Android | `:app:androidApp` | `./gradlew :app:androidApp:assembleDebug` leaves the APK in `app/androidApp/build/outputs/apk/debug/` |
| iOS | `app/iosApp` (Xcode project) | open the project in Xcode and run it from there |
| Desktop | `:app:desktopApp` | `./gradlew :app:desktopApp:run` |
| Web | `:app:webApp` | `./gradlew :app:webApp:wasmJsBrowserDistribution` leaves the bundle in `app/webApp/build/dist/wasmJs/productionExecutable` |

The `app/shared` module is Gradle project `:app:shared`. The iOS app is the only one not built by
Gradle, because it needs Xcode to link the framework from that module.

The Android `versionCode` and `versionName` come from `-PversionNum` when you pass it. For a release
build, the keystore is read from `keystore.properties`.

`./gradlew :app:androidApp:assembleRelease` runs R8 with `app/androidApp/proguard-rules.pro` and writes an
`mapping.txt` under `app/androidApp/build/outputs/mapping/release/`. Keep that file: it is the only way to
read a release crash, and it must never be published because it undoes the obfuscation. The debug
variant carries an `.debug` application id suffix, so both can be installed side by side.

Spotless keeps the build tidy: ktlint enforces the style. `./gradlew spotlessCheck` verifies it and
`./gradlew spotlessApply` rewrites whatever does not comply.

# Tests

Data-layer dependencies are exposed as interfaces. Tests can replace their production implementations
with lightweight fakes, all kept in `core:testing`.

The suite covers domain rules, mappers and view models. Integration tests also exercise the real
repositories against a stubbed HTTP backend, following the data through to the view model.

Platform-independent tests live in `commonTest` and run on each supported target. Tests that need the
stubbed backend live in `jvmTest`; they use the real preferences implementation, which stores its data
in a file on the JVM.

## Why fakes and not mocks

There are two reasons.

The technical one is that a mock is built at runtime, and that depends on the JVM: Kotlin/Native links
the binary ahead of time and wasm has no reflection. With MockK, those tests only compile for the JVM
and the other platforms are left with an empty task, silently. There are libraries that generate
fakes with an annotation processor or a compiler plugin, but that is another piece in the build to
get what a hand-written file already does
([well explained here](https://blogs.mobilebytesensei.com/blog/a-fake-beats-a-mock-in-commontest/)).

The design one is the same [Now in Android](https://github.com/android/nowinandroid#testing) gives: a
mock checks **how** the code talks to its dependency (which method, how many times, in what order), and
that ties the test to the implementation; any refactor breaks it even when the behaviour is unchanged. A
fake behaves like the real thing but simplified, with hooks to set up the scenario, and the check is on
the state the user would see.

## Where they run

| Where | Command |
|---|---|
| JVM, every module | `./gradlew jvmTest` |
| JVM, one module | `./gradlew :feature:tracking:jvmTest` |
| Android host | `./gradlew testAndroidHostTest`, only `shared` |
| Android device | `./gradlew :core:database:connectedAndroidDeviceTest`, with the phone plugged in and USB debugging on |
| iOS simulator | `./gradlew :app:shared:iosSimulatorArm64Test`, on an Apple Silicon Mac |
| Browser | `./gradlew wasmJsBrowserTest`, through Karma |

Set `LIVE_API=1` to include the JVM probes that call the live service:

```bash
LIVE_API=1 ./gradlew jvmTest
```

Without this variable, those probes are skipped, so the regular test run does not require a network
connection.

# License
Designed and developed by Adrián Cabrera, 2026.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
