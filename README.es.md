[English](README.md) · **Español**

Aplicación Logroño Bus
=====================

![kotlin-version](https://img.shields.io/badge/kotlin-2.4.21-blue?logo=kotlin)
![compose-multiplatform](https://img.shields.io/badge/compose%20multiplatform-1.13.0--beta01-blue)
![license](https://img.shields.io/badge/license-Apache%202.0-blue)
![build](https://github.com/Adrc95/logrono-bus/actions/workflows/build.yml/badge.svg)
[![web demo](https://img.shields.io/badge/demo-adrc95.github.io%2Flogrono--bus-ff2d55)](https://adrc95.github.io/logrono-bus/)

Logroño Bus te ayuda a moverte en autobús por Logroño. Consulta las próximas salidas de tus paradas
guardadas, encuentra paradas cercanas, sigue los autobuses en el mapa y busca en toda la red. También
puedes usar la cámara para escanear el código de una parada.

> [!IMPORTANT]
> Es un proyecto personal y **no oficial**. No está vinculado al Ayuntamiento de Logroño ni a la empresa
> que gestiona los autobuses. La app utiliza el servicio público del Ayuntamiento para consultar los
> horarios (GTFS) y las actualizaciones en tiempo real (GTFS-Realtime).

La interfaz está escrita una sola vez con Compose Multiplatform y corre en Android, iOS, escritorio y
web.

Si quieres ver cómo está organizado el código, hay dos documentos: la
[guía de modularización](docs/ModularizationLearningJourney.es.md) y la
[guía de arquitectura](docs/ArchitectureLearningJourney.es.md).

# Funcionalidades

- **Paradas guardadas.** Consulta las próximas salidas de las paradas que sigues, con el color de
  cada línea y sus retrasos.
- **Paradas cercanas.** Encuentra las paradas más próximas y calcula cuánto tardarías en llegar a pie.
- **Seguimiento en directo.** Localiza un autobús en el mapa, sigue su recorrido y consulta la hora
  prevista de llegada a cada parada. El seguimiento termina cuando llega a la cabecera.
- **Buscador de paradas.** Busca por nombre y consulta las salidas de la parada.
- **Escáner.** Escanea con la cámara el código de la marquesina que aparece en la parada.
- **Autobuses, líneas y tarifas.** Consulta qué autobuses están circulando, explora las líneas de la
  red y revisa las tarifas.

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

El proyecto se compila con el wrapper de Gradle incluido. Antes de empezar, instala estas herramientas:

- **JDK 17**: es la versión de Java que usan todos los módulos.
- **Android Studio** y **Android SDK 37**: necesarios para Android y también para los targets de
  escritorio y web.
- **Xcode**: necesario para compilar y ejecutar la app de iOS, ubicada en [`app/iosApp`](./app/iosApp).

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

El código se organiza en las capas de modelo, dominio, datos y presentación. Las dependencias avanzan
en una sola dirección para que cada capa tenga una responsabilidad clara. La interfaz compartida, hecha
con Compose Multiplatform, se reparte entre el módulo del sistema de diseño y el de componentes de UI.

La [guía de arquitectura](docs/ArchitectureLearningJourney.es.md) explica cada capa y sus límites.

# Modularización

Los módulos de Gradle se agrupan en tres tipos: bibliotecas `core`, funcionalidades y aplicaciones.
El anfitrión de la interfaz compartida y las aplicaciones de plataforma se agrupan en `app/`:
`androidApp`, `webApp` y `desktopApp` son módulos Gradle, mientras que `iosApp` sigue siendo un
proyecto nativo de Xcode. Los plugins de convención mantienen coherentes la configuración de plugins,
targets y dependencias. Cada módulo incluye su propio grafo de dependencias en el README.

Consulta la [guía de modularización](docs/ModularizationLearningJourney.es.md) para más detalles.

# Compilar

| Plataforma | Módulo | Cómo se ejecuta |
|---|---|---|
| Android | `:app:androidApp` | `./gradlew :app:androidApp:assembleDebug` deja el APK en `app/androidApp/build/outputs/apk/debug/` |
| iOS | `app/iosApp` (proyecto Xcode) | abre el proyecto en Xcode y ejecútalo desde ahí |
| Escritorio | `:app:desktopApp` | `./gradlew :app:desktopApp:run` |
| Web | `:app:webApp` | `./gradlew :app:webApp:wasmJsBrowserDistribution` deja el paquete en `app/webApp/build/dist/wasmJs/productionExecutable` |

El módulo `app/shared` tiene la ruta Gradle `:app:shared`. La app de iOS es la única que no compila
Gradle, porque necesita Xcode para enlazar el framework de ese módulo.

El `versionCode` y el `versionName` de Android salen de `-PversionNum` cuando lo pasas. Para una build
de release, el keystore se lee de `keystore.properties`.

`./gradlew :app:androidApp:assembleRelease` aplica R8 con `app/androidApp/proguard-rules.pro` y genera un
`mapping.txt` en `app/androidApp/build/outputs/mapping/release/`. Guarda ese fichero: es la única forma de
leer un crash de release, y no debe publicarse nunca porque deshace la ofuscación. La variante debug
lleva el sufijo `.debug` en el identificador, así que las dos se pueden instalar a la vez.

Spotless mantiene el build ordenado: ktlint impone el estilo. `./gradlew spotlessCheck` lo verifica y
`./gradlew spotlessApply` reescribe lo que no cumpla.

# Tests

Las dependencias de la capa de datos se definen mediante interfaces. Así, los tests pueden sustituir
las implementaciones reales por fakes sencillos, que se guardan en `core:testing`.

La suite cubre las reglas del dominio, los mappers y los view models. También hay tests de integración
que prueban los repositorios reales con un backend HTTP simulado y siguen los datos hasta el view model.

Los tests independientes de la plataforma están en `commonTest` y se ejecutan en cada target
compatible. Los que necesitan el backend simulado están en `jvmTest`; utilizan la implementación real
de preferencias, que en la JVM guarda los datos en un fichero.

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
| Simulador de iOS | `./gradlew :app:shared:iosSimulatorArm64Test`, en un Mac con Apple Silicon |
| Navegador | `./gradlew wasmJsBrowserTest`, con Karma |

Para incluir los probes de JVM que consultan el servicio real, define `LIVE_API=1`:

```bash
LIVE_API=1 ./gradlew jvmTest
```

Si no defines esa variable, los probes se omiten y la ejecución habitual de tests no necesita conexión
a internet.

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
