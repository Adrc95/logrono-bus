[English](README.md) · **Español**

Aplicación Logroño Bus
=====================

![kotlin-version](https://img.shields.io/badge/kotlin-2.4.21-blue?logo=kotlin)
![compose-multiplatform](https://img.shields.io/badge/compose%20multiplatform-1.13.0--alpha02-blue)
![license](https://img.shields.io/badge/license-Apache%202.0-blue)
![build](https://github.com/Adrc95/logrono-bus/actions/workflows/build.yml/badge.svg)
[![web demo](https://img.shields.io/badge/demo-adrc95.github.io%2Flogrono--bus-ff2d55)](https://adrc95.github.io/logrono-bus/)

Logroño Bus es una app para consultar los autobuses urbanos de Logroño. Muestra las próximas salidas
de las paradas que guardas en tus favoritos, las paradas que tienes cerca, la posición del bus en el
mapa y un buscador con todas las paradas de la red. También puede leer el código de una marquesina
con la cámara.

> [!IMPORTANT]
> Es un proyecto personal y **no oficial**. No tiene relación con el Ayuntamiento de Logroño ni con la
> empresa que gestiona los autobuses. Los datos salen del servicio web público del Ayuntamiento, con
> horarios GTFS y avisos GTFS-Realtime.

La interfaz está escrita una sola vez con Compose Multiplatform y corre en Android, iOS, escritorio y
web.

Si quieres ver cómo está organizado el código, hay dos documentos: la
[guía de modularización](docs/ModularizationLearningJourney.es.md) y la
[guía de arquitectura](docs/ArchitectureLearningJourney.es.md).

# Funcionalidades

- **Paradas guardadas.** Las próximas salidas de cada parada que sigues, con el color de la línea y
  el retraso.
- **Cerca de ti.** Las paradas más cercanas y lo que se tarda en llegar andando.
- **Seguimiento.** El autobús en el mapa, con su recorrido y las horas de llegada a cada parada. Al
  llegar a cabecera, el viaje se cierra.
- **Buscador.** Busca una parada por nombre y abre sus salidas.
- **Escáner.** Lee el código de la marquesina con la cámara.
- **Autobuses, líneas y tarifas.** Qué autobuses están circulando, las líneas de la red y las tarifas.

## Capturas

**Android (Jetpack Compose)**

<table>
  <tr>
    <td><img src="screenshots/android-mystops.png" width="200" height="407" alt="Paradas guardadas con sus próximas salidas"/></td>
    <td><img src="screenshots/android-nearby.png" width="200" height="407" alt="Paradas cercanas"/></td>
    <td><img src="screenshots/android-tracking.png" width="200" height="407" alt="Seguimiento del bus en directo"/></td>
    <td><img src="screenshots/android-search.png" width="200" height="407" alt="Buscador de paradas"/></td>
  </tr>
  <tr>
    <td align="center">Paradas guardadas</td>
    <td align="center">Cerca de ti</td>
    <td align="center">Seguimiento</td>
    <td align="center">Buscador</td>
  </tr>
</table>

**iOS (Compose Multiplatform)**

<table>
  <tr>
    <td><img src="screenshots/ios-mystops.png" width="200" height="435" alt="Paradas guardadas con sus próximas salidas"/></td>
    <td><img src="screenshots/ios-nearby.png" width="200" height="435" alt="Paradas cercanas"/></td>
    <td><img src="screenshots/ios-tracking.png" width="200" height="435" alt="Seguimiento del bus en directo"/></td>
    <td><img src="screenshots/ios-search.png" width="200" height="435" alt="Buscador de paradas"/></td>
  </tr>
  <tr>
    <td align="center">Paradas guardadas</td>
    <td align="center">Cerca de ti</td>
    <td align="center">Seguimiento</td>
    <td align="center">Buscador</td>
  </tr>
</table>

La misma interfaz corre también en escritorio y en web.

# Entorno de desarrollo

El proyecto usa Gradle a través del wrapper. Necesitas:

- **JDK 17**, que es el toolchain de todos los módulos.
- **Android Studio** con el SDK de Android 37, para la app de Android, la de escritorio y la web.
- **Xcode**, para la de iOS (el proyecto está en [`/iosApp`](./iosApp)).

Abre la raíz del repositorio en el IDE y deja que importe el proyecto. Las versiones que importan
están en [`gradle/libs.versions.toml`](./gradle/libs.versions.toml).

# Librerías

| Librería | Para qué |
|---|---|
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | El código compartido entre Android, iOS, escritorio y web |
| [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) | La interfaz, escrita una sola vez |
| [Material 3](https://m3.material.io/) | El sistema visual, con sus variantes adaptativas |
| [AndroidX Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | El view model y el estado que observa la pantalla |
| [Activity Compose](https://developer.android.com/jetpack/androidx/releases/activity) | El punto de entrada de Android |
| [Koin](https://insert-koin.io/) | Inyección de dependencias resuelta al compilar |
| [Ktor](https://ktor.io/) | Cliente HTTP, con el motor de cada plataforma |
| [Ktorfit](https://github.com/Foso/Ktorfit) | Los endpoints, declarados como interfaces |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | JSON y claves de navegación |
| [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) | Concurrencia y flujos |
| [kotlinx-datetime](https://github.com/Kotlin/kotlinx-datetime) | Fechas y horas |
| [kotlinx-collections-immutable](https://github.com/Kotlin/kotlinx.collections.immutable) | Colecciones inmutables en el estado |
| [kotlinx-io](https://github.com/Kotlin/kotlinx-io) | Bytes y ficheros, por debajo de DataStore |
| [kotlinx-browser](https://github.com/Kotlin/kotlinx-browser) | APIs del navegador en el target web |
| [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) | La pila de pantallas, que se guarda |
| [NavigationEvent](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture) | El gesto de volver atrás |
| [Room](https://developer.android.com/kotlin/multiplatform/room) | La base de datos local, con el driver nativo o el del navegador |
| [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) | Las preferencias |
| [MapLibre Compose](https://github.com/maplibre/maplibre-compose) | El mapa en Android, iOS y web |
| [JetLime](https://github.com/pushpalroy/jetlime) | La línea de tiempo del seguimiento |
| [CameraX](https://developer.android.com/media/camera/camerax) | La cámara del escáner, en Android |
| [ML Kit](https://developers.google.com/ml-kit/vision/text-recognition/v2) | Leer el código de la marquesina en Android |
| [Vision](https://developer.apple.com/documentation/vision) | Leer el código de la marquesina en iOS |
| [Grant](https://github.com/brewkits/Grant) | Los permisos de cámara y ubicación |
| [Play Services Location](https://developers.google.com/android/reference/com/google/android/gms/location/package-summary) | La ubicación en Android |
| [CoreLocation](https://developer.apple.com/documentation/corelocation) | La ubicación en iOS |
| [Napier](https://github.com/AAkira/Napier) | Logs, con el registro de cada plataforma |
| [kotlin-test](https://kotlinlang.org/api/core/kotlin-test/) y [coroutines-test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/) | El marco de los tests |
| [Turbine](https://github.com/cashapp/turbine) | Comprobar flujos en los tests |

# Arquitectura

El código está separado en capas: modelo, dominio, datos y presentación, con las dependencias en una
sola dirección. La interfaz, escrita una vez con Compose Multiplatform, vive en dos módulos: el
sistema de diseño y los componentes compartidos.

Cómo está organizada cada capa, qué puede hacer y qué no, está en la
[guía de arquitectura](docs/ArchitectureLearningJourney.es.md).

# Modularización

El proyecto está repartido en módulos de Gradle de tres tipos: `core`, funcionalidades y aplicaciones.
Casi todos comparten plugins, targets y dependencias mediante plugins de convención, y cada módulo
lleva su grafo de dependencias en su propio README.

El detalle está en la [guía de modularización](docs/ModularizationLearningJourney.es.md).

# Compilar

| Plataforma | Módulo | Cómo se ejecuta |
|---|---|---|
| Android | `:androidApp` | `./gradlew :androidApp:assembleDebug` deja el APK en `androidApp/build/outputs/apk/debug/` |
| iOS | `/iosApp` | abre el proyecto en Xcode y ejecútalo desde ahí |
| Escritorio | `:desktopApp` | `./gradlew :desktopApp:run` |
| Web | `:webApp` | `./gradlew :webApp:wasmJsBrowserDistribution` deja el paquete en `webApp/build/dist/wasmJs/productionExecutable` |

La app de iOS es la única que no compila Gradle, porque necesita Xcode para enlazar el framework.

El `versionCode` y el `versionName` de Android salen de `-PversionNum` cuando lo pasas. Para una build
de release, el keystore se lee de `keystore.properties`.

Spotless mantiene el build ordenado: ktlint impone el estilo. `./gradlew spotlessCheck` lo verifica y
`./gradlew spotlessApply` reescribe lo que no cumpla.

# Tests

Los componentes de datos se declaran como interfaces, así que un test puede sustituir la implementación
real por un fake sin tocar el código de producción. Los fakes viven solo en `core:testing`.

Se prueba cada capa: las reglas del dominio, los mappers y los view models. Además, las pruebas de
integración usan los repositorios reales contra un backend simulado y llegan hasta el view model.

Los tests que son para cualquier plataforma están en `commonTest`, y se ejecutan en todas. Los que usan
el backend simulado están en `jvmTest`: ese backend monta la app entera, con las preferencias reales, y
en la JVM esas preferencias escriben en un fichero.

## Por qué fakes y no mocks

Hay dos motivos.

El técnico es que un mock se construye en tiempo de ejecución, y eso depende de la JVM: en Kotlin/Native
el binario se enlaza antes de ejecutarse y en wasm no hay reflexión. Con MockK, esos tests sólo compilan
para la JVM y las demás plataformas se quedan con la tarea vacía, sin avisar. Existen librerías que
generan fakes con un procesador de anotaciones o un plugin del compilador, pero es otra pieza en el
build para conseguir lo que un fichero escrito a mano ya hace
([aquí se explica bien](https://blogs.mobilebytesensei.com/blog/a-fake-beats-a-mock-in-commontest/)).

El de diseño es el mismo que da [Now in Android](https://github.com/android/nowinandroid#testing): un
mock comprueba **cómo** habla el código con su dependencia (qué método, cuántas veces, en qué orden), y
eso ata el test a la implementación; cualquier refactor lo rompe aunque el comportamiento no cambie. Un
fake se comporta como la real pero simplificado, con ganchos para montar el escenario, y la comprobación
es sobre el estado que vería el usuario.

## Dónde se ejecuta

| Dónde | Comando |
|---|---|
| JVM, todos los módulos | `./gradlew jvmTest` |
| JVM, un módulo | `./gradlew :feature:tracking:jvmTest` |
| Host de Android | `./gradlew testAndroidHostTest`, sólo `shared` |
| Móvil Android | `./gradlew :core:database:connectedAndroidDeviceTest`, con el móvil conectado y depurando por USB |
| Simulador de iOS | `./gradlew :shared:iosSimulatorArm64Test`, en un Mac con Apple Silicon |
| Navegador | `./gradlew wasmJsBrowserTest`, con Karma |

Con `LIVE_API=1 ./gradlew jvmTest` se añaden las pruebas que van contra el servicio real. Sin esa
variable se saltan, para que el build normal no dependa de la red.

# Licencia
Desarrollado por Adrián Cabrera, 2026.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
