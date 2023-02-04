# Korge-Fleks

This is the [Fleks Entity Components System](https://github.com/Quillraven/Fleks) integration for KorGE Game Engine.
Korge-fleks consists of a growing set of component definitions and dedicated systems.

with some supporting stuff like AssetStore, etc. which are reusable or will get better reusable over time.
Eventually this will grow into a specialized game engine for 2D platform games or similar.
It depends what the ECS systems will be able to do.

Upstream project for Fleks ECS can be found here: <https://github.com/Quillraven/Fleks>

Maintained by [@jobe-m](https://github.com/jobe-m)

## Suported Version-tripple

- Korge-Fleks Addon: 0.0.1
- Korge: 3.4.0
- Fleks: 2.2

## Setup

As a clean start the [Korge-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world) repo can be used. It contains the kproject and gradle setup to use Fleks, Korge-Fleks and Korge together in a project.

In detail the project setup looks like that:

### `build.gradle.kts`

This tells gradle that the overall project depends on a project _deps_ in the root directory.

```kotlin
[...]
dependencies {
	add("commonMainApi", project(":deps"))
}
```

### `deps.kproject.yml`

This is the configuration for kproject to setup a project _deps_ in the root directory.
It just contains two dependencies to further projects in the `libs` sub-folder.

```kotlin
dependencies:
  - ./libs/fleks
  - ./libs/korge-fleks
```

### `settings.gradle.kts`

```kotlin
pluginManagement { repositories { mavenLocal(); mavenCentral(); google(); gradlePluginPortal() } }

plugins {
    id("com.soywiz.kproject.settings") version "0.0.6"
}

kproject("./deps")
```

### `libs/fleks.kproject.yml`

```kotlin
name: fleks
type: library

# loading git tag release from GitHub repo (https://github.com/Quillraven/Fleks)
src: git::Quillraven/Fleks::/src::2.2
# using Fleks sources locally in sub-folder "libs/fleks-src"
#src: ./fleks-src

```

### `libs/korge-fleks.kproject.yml`

```kotlin
name: korge-fleks
type: library

# loading git tag from GitHub repo (https://github.com/korlibs/korge-fleks)
src: git::korlibs/korge-fleks::/korge-fleks/src::0.0.1
# using Korge-Fleks sources locally in sub-folder "libs/korge-fleks"
#src: ./korge-fleks-src/korge-fleks

dependencies:
  - "maven::common::com.soywiz.korlibs.korge2:korge:3.4.0"
  - "./fleks"
```

### Updating to newer versions of KorGE-Fleks

It is important to understand that the Korge-Fleks Addon depends on specific versions of Korge and Fleks ECS. Thus, updating the version of Korge-Fleks also involves updating of Korge and Fleks versions. Do not try to update only one version until you know what you are doing.

The current tripple of versions which are working together can be seen at the top of this readme in section "Supported Version-tripple".

The Korge, Fleks ECS and Korge-Fleks versions need to be updated in three places:

#### `gradle/libs.versions.toml`

```kotlin
[plugins]
korge = { id = "com.soywiz.korge", version = "3.4.0" }
```

#### `libs/fleks.kproject.yml`

```kotlin
[...]
src: git::Quillraven/Fleks::/src::2.2
```

#### `libs/korge-fleks.kproject.yml`

```kotlin
[...]
src: git::korlibs/korge-fleks::/korge-fleks/src::0.0.1
[...]
dependencies:
  - "maven::common::com.soywiz.korlibs.korge2:korge:3.4.0"
```
... to be continued


## Usage

This repo contains under `src` folder the `korgeFleks` addon. It simplifies usage of Fleks in a KorGE environment.
For that a set of Components and Systems are implemented.

### Components

... to be continued

### Systems

... to be continued

### Fleks World Integration into a KorGE Scene

... to be continued

## Examples

* [Example in this repo](https://github.com/korlibs/korge-fleks/tree/main/example)

  <img width="546" alt="Screenshot 2022-10-26 at 13 54 12" src="https://user-images.githubusercontent.com/570848/198019508-dafdb3a5-02af-49f7-92ec-9f76533c2524.png">

* [Korge-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world)

## History

* <https://github.com/korlibs-archive/korge-next/pull/472>
* <https://github.com/korlibs/korge/pull/988>
