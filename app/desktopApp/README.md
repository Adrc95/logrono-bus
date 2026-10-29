# `:app:desktopApp`

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
  subgraph :app
    direction TB
    :app:desktopApp[desktopApp]:::jvm-library
    :app:shared[shared]:::library
  end
  subgraph :core
    direction TB
    :core:common[common]:::library
    :core:data[data]:::library
    :core:database[database]:::library
    :core:designsystem[designsystem]:::library
    :core:device[device]:::library
    :core:domain[domain]:::library
    :core:model[model]:::library
    :core:navigation[navigation]:::library
    :core:network[network]:::library
    :core:preference[preference]:::library
    :core:ui[ui]:::library
  end
  subgraph :feature
    direction TB
    :feature:buses[buses]:::feature
    :feature:fares[fares]:::feature
    :feature:favourites[favourites]:::feature
    :feature:nearby[nearby]:::feature
    :feature:routes[routes]:::feature
    :feature:scan[scan]:::feature
    :feature:settings[settings]:::feature
    :feature:stops[stops]:::feature
    :feature:tracking[tracking]:::feature
    subgraph :feature:buses
      direction TB
      :feature:buses:navigation[navigation]:::navigation
    end
    subgraph :feature:fares
      direction TB
      :feature:fares:navigation[navigation]:::navigation
    end
    subgraph :feature:favourites
      direction TB
      :feature:favourites:navigation[navigation]:::navigation
    end
    subgraph :feature:nearby
      direction TB
      :feature:nearby:navigation[navigation]:::navigation
    end
    subgraph :feature:routes
      direction TB
      :feature:routes:navigation[navigation]:::navigation
    end
    subgraph :feature:scan
      direction TB
      :feature:scan:navigation[navigation]:::navigation
    end
    subgraph :feature:settings
      direction TB
      :feature:settings:navigation[navigation]:::navigation
    end
    subgraph :feature:stops
      direction TB
      :feature:stops:navigation[navigation]:::navigation
    end
    subgraph :feature:tracking
      direction TB
      :feature:tracking:navigation[navigation]:::navigation
    end
  end

  :app:desktopApp -.-> :app:shared
  :app:shared -.-> :core:common
  :app:shared -.-> :core:data
  :app:shared -.-> :core:database
  :app:shared -.-> :core:designsystem
  :app:shared -.-> :core:device
  :app:shared -.-> :core:domain
  :app:shared -.-> :core:model
  :app:shared -.-> :core:navigation
  :app:shared -.-> :core:network
  :app:shared -.-> :core:preference
  :app:shared -.-> :core:ui
  :app:shared -.-> :feature:buses
  :app:shared -.-> :feature:fares
  :app:shared -.-> :feature:favourites
  :app:shared -.-> :feature:nearby
  :app:shared -.-> :feature:routes
  :app:shared -.-> :feature:scan
  :app:shared -.-> :feature:settings
  :app:shared -.-> :feature:stops
  :app:shared -.-> :feature:tracking
  :core:data --> :core:common
  :core:data -.-> :core:database
  :core:data -.-> :core:device
  :core:data --> :core:domain
  :core:data --> :core:model
  :core:data -.-> :core:network
  :core:data -.-> :core:preference
  :core:database --> :core:model
  :core:device -.-> :core:common
  :core:device --> :core:model
  :core:domain --> :core:common
  :core:domain --> :core:model
  :core:navigation -.-> :core:common
  :core:network --> :core:common
  :core:network -.-> :core:domain
  :core:network --> :core:model
  :core:preference -.-> :core:common
  :core:preference --> :core:model
  :core:ui -.-> :core:common
  :core:ui --> :core:designsystem
  :core:ui -.-> :core:domain
  :core:ui --> :core:model
  :feature:buses -.-> :core:common
  :feature:buses -.-> :core:designsystem
  :feature:buses -.-> :core:domain
  :feature:buses --> :core:model
  :feature:buses -.-> :core:navigation
  :feature:buses -.-> :core:ui
  :feature:buses --> :feature:buses:navigation
  :feature:buses -.-> :feature:fares:navigation
  :feature:buses -.-> :feature:stops:navigation
  :feature:fares -.-> :core:common
  :feature:fares -.-> :core:designsystem
  :feature:fares -.-> :core:domain
  :feature:fares --> :core:model
  :feature:fares -.-> :core:navigation
  :feature:fares -.-> :core:ui
  :feature:fares --> :feature:fares:navigation
  :feature:favourites -.-> :core:common
  :feature:favourites -.-> :core:designsystem
  :feature:favourites -.-> :core:domain
  :feature:favourites --> :core:model
  :feature:favourites -.-> :core:navigation
  :feature:favourites -.-> :core:ui
  :feature:favourites --> :feature:favourites:navigation
  :feature:favourites -.-> :feature:routes:navigation
  :feature:favourites -.-> :feature:stops:navigation
  :feature:favourites -.-> :feature:tracking:navigation
  :feature:nearby -.-> :core:common
  :feature:nearby -.-> :core:designsystem
  :feature:nearby -.-> :core:domain
  :feature:nearby --> :core:model
  :feature:nearby -.-> :core:navigation
  :feature:nearby -.-> :core:ui
  :feature:nearby --> :feature:nearby:navigation
  :feature:nearby -.-> :feature:stops:navigation
  :feature:routes -.-> :core:common
  :feature:routes -.-> :core:designsystem
  :feature:routes -.-> :core:domain
  :feature:routes --> :core:model
  :feature:routes -.-> :core:navigation
  :feature:routes -.-> :core:ui
  :feature:routes --> :feature:routes:navigation
  :feature:routes -.-> :feature:settings:navigation
  :feature:routes -.-> :feature:stops:navigation
  :feature:routes -.-> :feature:tracking:navigation
  :feature:scan -.-> :core:common
  :feature:scan -.-> :core:designsystem
  :feature:scan -.-> :core:device
  :feature:scan -.-> :core:domain
  :feature:scan --> :core:model
  :feature:scan -.-> :core:navigation
  :feature:scan -.-> :core:ui
  :feature:scan --> :feature:scan:navigation
  :feature:scan -.-> :feature:stops:navigation
  :feature:settings -.-> :core:common
  :feature:settings -.-> :core:designsystem
  :feature:settings -.-> :core:domain
  :feature:settings --> :core:model
  :feature:settings -.-> :core:navigation
  :feature:settings -.-> :core:ui
  :feature:settings --> :feature:settings:navigation
  :feature:stops -.-> :core:common
  :feature:stops -.-> :core:designsystem
  :feature:stops -.-> :core:domain
  :feature:stops --> :core:model
  :feature:stops -.-> :core:navigation
  :feature:stops -.-> :core:ui
  :feature:stops --> :feature:stops:navigation
  :feature:stops -.-> :feature:tracking:navigation
  :feature:tracking -.-> :core:common
  :feature:tracking -.-> :core:designsystem
  :feature:tracking -.-> :core:domain
  :feature:tracking --> :core:model
  :feature:tracking -.-> :core:navigation
  :feature:tracking -.-> :core:ui
  :feature:tracking --> :feature:tracking:navigation

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
  jvm-library[jvm-library]:::jvm-library
  kmp-library[kmp-library]:::kmp-library
  android-library[android-library]:::android-library

  feature --> navigation
  feature --> library
  application --> library
  library -.-> jvm-library

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
