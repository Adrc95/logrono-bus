# `:feature:stops`

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
    :feature:stops[stops]:::feature
    subgraph :feature:stops
      direction TB
      :feature:stops:navigation[navigation]:::navigation
    end
    subgraph :feature:tracking
      direction TB
      :feature:tracking:navigation[navigation]:::navigation
    end
  end
  subgraph :core
    direction TB
    :core:common[common]:::library
    :core:designsystem[designsystem]:::library
    :core:domain[domain]:::library
    :core:model[model]:::library
    :core:navigation[navigation]:::library
    :core:ui[ui]:::library
  end

  :core:domain --> :core:common
  :core:domain --> :core:model
  :core:navigation -.-> :core:common
  :core:ui -.-> :core:common
  :core:ui --> :core:designsystem
  :core:ui -.-> :core:domain
  :core:ui --> :core:model
  :feature:stops -.-> :core:common
  :feature:stops -.-> :core:designsystem
  :feature:stops -.-> :core:domain
  :feature:stops --> :core:model
  :feature:stops -.-> :core:navigation
  :feature:stops -.-> :core:ui
  :feature:stops --> :feature:stops:navigation
  :feature:stops -.-> :feature:tracking:navigation

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
