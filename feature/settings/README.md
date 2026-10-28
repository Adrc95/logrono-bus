# `:feature:settings`

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
  subgraph :feature
    direction TB
    :feature:settings[settings]:::feature
    subgraph :feature:settings
      direction TB
      :feature:settings:navigation[navigation]:::navigation
    end
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
  :core:navigation -.-> :core:common
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
  :feature:settings -.-> :core:common
  :feature:settings -.-> :core:designsystem
  :feature:settings -.-> :core:domain
  :feature:settings --> :core:model
  :feature:settings -.-> :core:navigation
  :feature:settings -.-> :core:testing
  :feature:settings -.-> :core:ui
  :feature:settings --> :feature:settings:navigation

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
