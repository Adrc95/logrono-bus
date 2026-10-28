# `:core:domain`

## Qué hay aquí

La lógica de los datos del bus: **reglas puras** y **casos de uso**. No sabe de Android, de Compose
ni de pantallas; sólo de líneas, paradas, viajes y horas.

| Paquete | Qué contiene |
|---|---|
| `usecase/` | los **casos de uso**: la puerta del dominio. Cada uno pregunta algo ("dame las paradas de esta línea", "dame el bus que sigue el usuario") y devuelve el dato ya calculado. |
| `trip/` | las **reglas del viaje**: qué sentido va un bus, qué hora tiene cada parada, qué viaje es el que sigue el usuario, cuándo el feed está viejo, cómo se une la flota con el horario. |
| `geo/` | las **reglas de posición**: qué paradas hay cerca, a qué distancia están, por dónde va el bus. |
| `scan/` | la regla de **reconocer una parada** en un texto (el código QR). |
| `repository/` | los **contratos** de lo que el dominio necesita del exterior (catálogo, vehículos, horarios, favoritos, ajustes…). Los implementa `core:data`. |
| `di/` | el módulo de Koin del dominio. |
| (raíz) | `DomainConstants` (las tolerancias y plazos que usan varias reglas) y los `Repository` de contrato. |

## La regla de oro

> Si sólo depende de modelos de dominio y se puede testear sin falsificar una fuente, es dominio.

Por eso las reglas viven en `trip/` y `geo/` (funciones puras, con sus tests en `commonTest`) y no
en las pantallas: una pantalla no decide a qué hora pasa un bus.

## Module dependency graph

<!--region graph-->
```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
  subgraph :core
    direction TB
    :core:common[common]:::library
    :core:data[data]:::library
    :core:database[database]:::library
    :core:designsystem[designsystem]:::library
    :core:device[device]:::library
    :core:domain[domain]:::library
    :core:model[model]:::library
    :core:network[network]:::library
    :core:preference[preference]:::library
    :core:testing[testing]:::library
    :core:ui[ui]:::library
  end

  :core:data --> :core:common
  :core:data -.-> :core:database
  :core:data -.-> :core:device
  :core:data --> :core:domain
  :core:data --> :core:model
  :core:data -.-> :core:network
  :core:data -.-> :core:preference
  :core:data -.-> :core:testing
  :core:database --> :core:model
  :core:database -.-> :core:testing
  :core:device -.-> :core:common
  :core:device --> :core:model
  :core:domain --> :core:common
  :core:domain --> :core:model
  :core:domain -.-> :core:testing
  :core:network --> :core:common
  :core:network -.-> :core:domain
  :core:network --> :core:model
  :core:network -.-> :core:testing
  :core:preference -.-> :core:common
  :core:preference --> :core:model
  :core:preference -.-> :core:testing
  :core:testing --> :core:data
  :core:testing --> :core:database
  :core:testing --> :core:domain
  :core:testing --> :core:model
  :core:testing --> :core:network
  :core:testing --> :core:preference
  :core:testing --> :core:ui
  :core:ui -.-> :core:common
  :core:ui --> :core:designsystem
  :core:ui -.-> :core:domain
  :core:ui --> :core:model
  :core:ui -.-> :core:testing

classDef feature fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef navigation fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
classDef application fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#FDFFB6,stroke:#000,stroke-width:2px,color:#000;
classDef kmp-library fill:#FFC6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#E0E0E0,stroke:#000,stroke-width:2px,color:#000;
classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```

<details><summary>📋 Graph legend</summary>

```mermaid
graph TB
  feature[feature]:::feature
  navigation[navigation]:::navigation
  library[library]:::library
  application[application]:::application
  jvm[jvm]:::jvm-library

  feature --> navigation
  feature --> library
  application --> library
  library -.-> jvm

classDef feature fill:#CAFFBF,stroke:#000,stroke-width:2px,color:#000;
classDef navigation fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
classDef library fill:#BDB2FF,stroke:#000,stroke-width:2px,color:#000;
classDef application fill:#FFD6A5,stroke:#000,stroke-width:2px,color:#000;
classDef jvm-library fill:#FDFFB6,stroke:#000,stroke-width:2px,color:#000;
classDef kmp-library fill:#FFC6FF,stroke:#000,stroke-width:2px,color:#000;
classDef android-library fill:#E0E0E0,stroke:#000,stroke-width:2px,color:#000;
classDef unknown fill:#FFADAD,stroke:#000,stroke-width:2px,color:#000;
```

</details>
<!--endregion-->
