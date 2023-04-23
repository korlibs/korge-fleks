# Korge-Fleks

This is the [Fleks Entity Components System](https://github.com/Quillraven/Fleks) (ECS) integration for KorGE Game Engine.
Korge-fleks consists of a growing set of Entity Component definitions, dedicated Entity Systems and utilities around
them like AssetStore, Entity-Component serialization, etc. which are reusable or will get better reusable over time.
Eventually this will grow into a specialized Korge-based game engine for 2D platform games or similar.
It depends on what the ECS systems will be able to do.

Upstream project for Fleks ECS can be found here: <https://github.com/Quillraven/Fleks>

Korge-Fleks is maintained by [@jobe-m](https://github.com/jobe-m)

## Supported Versions

- Korge: 4.0.0-rc
- Korge-fleks addon: v0.0.4
- Korge-parallax addon: (no commit yet)
- Korge-tiled addon: 13e674655b94d422839fd3b689f8ba40e92fa84c
- Fleks: c24925091ced418bf045ba0672734addaab573d8 (on branch 2.3-korge-serialization)

## Idea

The Korge-Fleks implementation follows the idea to separate the configuration of Game Objects from its behaviour.
A Game Object in an ECS world is an Entity and by that just an index number (in Fleks e.g. `Entity(id = 1)`).
The aspects of an Entity are stored in Components. Aspects can be read as runtime-configuration for a Game Object.
Component objects have a relationship to at least one Entity.
ECS Systems iterate over all (active) Entities of an ECS world and execute the "behaviour" for each Entity.
To do so they use the config (aspects) from all associated Components of that Entity.

If I lost you already please step back and read elsewhere a little more about ECS basics. It is important to
understand the basic principles of an ECS system. Moreover, there are various interpretations out there what an
ECS is and how it works. But when you read further down you should get the idea of how the Fleks ECS can work
within a Korge game.

... to be continued

- Components contain only basic (nullable) types like Int, String, Boolean, Entity, invokable function prototypes
  (lambdas) and set of them in Lists and Maps
- Components do not contain any Korge-related complex objects like Views, Components, etc.
- Components are easily serializable because of its basic nature
- Save game can be done by simply serializing and saving the whole ECS world snapshot (all active entities
  and components)
- Loading a save game is done by deserializing a saved world snapshot
- ECS Systems keep track of complex Korge objects and map them to the Entities
- For simplicity all properties of a Component shall be independent of any Korge-specific type


## Setup

As a clean start the [Korge-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world) repo can be used.
It contains the kproject and gradle setup to use Fleks, Korge-Fleks and Korge together in a project.

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

```yaml
dependencies:
    - ./libs/fleks
    - ./libs/korge-fleks
```

### `settings.gradle.kts`

Needed settings for gradle to make kproject usable in the project.

```kotlin
pluginManagement { repositories { mavenLocal(); mavenCentral(); google(); gradlePluginPortal() } }

plugins {
    id("com.soywiz.kproject.settings") version "0.0.6"  // Newer versions are not yet working due to a bug in kproject
}

kproject("./deps")
```

### `libs/fleks.kproject.yml`

This is the kproject config for including Fleks sources into the Korge project.

```yaml
name: fleks
type: library

# loading git tag release (or commit) from GitHub repo (https://github.com/Quillraven/Fleks)
src: git::Quillraven/Fleks::/src::c24925091ced418bf045ba0672734addaab573d8  # on branch 2.3-korge-serialization
# using Fleks sources locally in sub-folder "libs/fleks-src"
#src: ./fleks-src

plugins:
  - serialization
```

### `libs/korge-fleks.kproject.yml`

This is the kproject config for including Korge-Fleks sources  into the Korge project.

```yaml
name: korge-fleks
type: library

# loading git tag from GitHub repo (https://github.com/korlibs/korge-fleks)
src: git::korlibs/korge-fleks::/korge-fleks/src::v0.0.3
# using Korge-Fleks sources locally in sub-folder "libs/korge-fleks"
#src: ./korge-fleks-src/korge-fleks

plugins:
  - serialization

dependencies:
  - "maven::common::com.soywiz.korlibs.korge2:korge:3.4.0"
  - "./fleks"
```

### Updating to newer versions of KorGE-Fleks

It is important to understand that Korge-Fleks depends on specific versions of Korge, Korge-parallax
addon, Korge-tiled addon and Fleks ECS.
Thus, updating the version of Korge-Fleks also involves updating all versions of those modules/addons.
Do not try to update only one version until you know what you are doing.

The current versions which are working together can be seen at the top of this readme in section
"Supported Versions".

The Korge, Fleks ECS and all Korge Addon versions need to be updated in following places:

#### Korge version

Korge version needs to be updated in `gradle/libs.versions.toml`:

```kotlin
[plugins]
korge = { id = "com.soywiz.korge", version = "4.0.0" }
```

#### Fleks version

Fleks ECS version needs to be updated in the kproject file `libs/fleks.kproject.yml`:

```
[...]
src: git::Quillraven/Fleks::/src::2.3
```

#### Korge Addon versions

All version of used Korge sddons (Korge-fleks, Korge-parallax, Korge-tiled) needs to be updated
in their corresponding kproject files `libs/korge-fleks.kproject.yml`, `libs/korge-parallax.kproject.yml` and
`libs/korge-tiled.kproject.yml`:

```kotlin
[...]
src: git::korlibs/korge-xxx::/korge-xxx/src::v0.0.x
```

## Usage

This repo contains under `korge-fleks/src` folder the `korgeFleks` addon. It simplifies usage of Fleks in a KorGE
environment. For that a set of Components and Systems are implemented.

### Components

... to be continued

### Serialization of Components

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
