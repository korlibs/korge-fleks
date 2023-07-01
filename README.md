# KorGE-Fleks

This is the [Fleks Entity Components System](https://github.com/Quillraven/Fleks) (ECS) integration for KorGE Game Engine.
KorGE-Fleks consists of a growing set of Entity Component definitions, dedicated Entity Systems and utilities around
them like AssetStore, Entity-Component serialization, etc. which are reusable or will get better reusable over time.
Eventually this will grow into a specialized KorGE-based Game Engine for 2D platform games.

Upstream project for Fleks ECS can be found here: <https://github.com/Quillraven/Fleks>

KorGE-Fleks is maintained by [@jobe-m](https://github.com/jobe-m)

# Supported Versions

This is a list of versions for all needed modules/addons which are known to work together:

- KorGE: v4.0.7
- KorGE-Fleks addon: v0.0.5
- KorGE-Parallax addon: b8e7356c3c5ba5fac273a83d3f6ef127a16de739 (on branch adaptation-of-parallax-view-to-korge-fleks)
- KorGE-Tiled addon: v0.0.2
- Fleks: ad6380dae7cfcd84a5406b2f9d1c1ca1ba2c4fb3 (on master - pre 2.4 release)

# Idea and Motivation

The KorGE-Fleks implementation follows the idea to separate the configuration of _Game Objects_ from their executed behavior.

A game object in an ECS is an _Entity_ and per definition just an index number (in Fleks e.g. `Entity(id = 1)`).
Runtime-configuration for a game object can be defined as an _Aspect_ of an entity. The aspects of an entity are stored in a
_Component_. An entity usually has multiple components assigned. _Systems_ iterate over all active entities of an ECS world
and execute the "behavior" for each Entity. To do so systems use the properties from all associated components of an entity.

If I lost you already please step back and read elsewhere a little more about ECS basics. It is important to
understand -at least- the basic principles of an ECS system. Moreover, there are various interpretations out there what
an ECS is and how it works. But when you read further down you should get the idea of how the Fleks ECS can work
within a KorGE game.

Per definition KorGE-Fleks components contain only data which is necessary to fully describe the state of a game
object at every point in time. All components are easily serializable with _kotlinx.serialization_ because of its
basic nature. This enables the game system to save the state of a game object and to restore its state
when needed. This also makes it possible to easily implement a save-game system or to handle transmission of game object
states over network for a multiplayer game. Details about implemented property types for components can be found below in
section Components.

KorGE-Fleks can save the state of the game by simply serializing and saving the whole ECS world snapshot as a JSON string
to a file (all active entities and components). Loading a saved state of the game is done by deserializing the
saved JSON string of a world
snapshot.

KorGE-Fleks implements a couple of useful Systems. Those systems keep track of complex KorGE objects and map them
to the Entities which needs those objects to implement various behavior like e.g. displaying sprites, playing sounds or
reacting to touch input.

Execution time of systems in Fleks is very static and thus predictable. The core of KorGE makes a lot of use of
coroutines and asyncronous execution of object's behavior in update functions. KorGE-Fleks tries to hide this
complexity in the implemented systems.
 
AssetStore implements loading of typical game asses like graphics images, sounds, level maps, game object configs, etc.
It uses up to 4 independently loadable sets of asset which can have different lifetime in the game:
- Common assets
- World assets
- Level assets
- Special assets

KorGe-Fleks provides basic entity configuration objects and functions to easily setup Game Objects 

- Parallax background
- Text and image logos
- Level maps (Tiledmap)
- Effect objects (like explosions)
- ... to be continued

In the end the actual game code outside KorGE-Fleks is just specialized configuration and individual assets for the
final Game Objects.

# Implementation of KorGE-Fleks

This repository contains under `korge-fleks/src` folder the _KorGE-Fleks_ addon. The next subsection gives an overview
over all provided Components and Systems. Also, Serialization of Entities and its associated components and how to
use the AssetStore and entity configuration to build up Game Objects is described.

## Components

All provided components in KorGe-Fleks contain only basic property types like:

- String
- Number (Int, Float, Double)
- Enum class (like Easing from KorGE)
- Entity (Int value class)
- Identifier (String value class for accessing static entity configuration which is loaded from assets and
  invoking functions with `World`, `Entity` and `Identifier` parameters)

Collections of above types in Lists and Maps are also supported.

For simplicity all properties are independent of any KorGE-specific complex classes. Components do not contain
any KorGE-related complex objects like `Views`, `Image`, `Camera`, etc.

Hint: Where it makes sense a basic type can be taken over from KorGE as it is done with the `Easing` enum class for the
_Animation system_.

## Serialization of Components

... to be continued

## Systems

...

## AssetStore

...

### Hot-reloading of Assets

...

## Fleks World Integration into a KorGE Scene

...

# Set up a new Game with KorGE-Fleks

As a clean start the [KorGE-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world) repository can be used.
It contains the kproject and gradle setup to use Fleks, KorGE and all needed KorGE addons _KorGE-Fleks, KorGE-Tiled,
KorGE-Parallax_ together in a project.

In detail the project setup looks like that:

## `build.gradle.kts`

This tells gradle that the overall project depends on a project _deps_ in the root directory.

```kotlin
[...]
dependencies {
  add("commonMainApi", project(":deps"))
}
```

## `deps.kproject.yml`

This is the configuration for kproject to set up a project _deps_ in the root directory.
It just contains one dependency to the KorGE-Fleks addon.

```yaml
dependencies:
  - https://github.com/korlibs/korge-fleks/tree/02fed30e752cc14c71cecafbdee2882c95d99e64/korge-fleks
# or a local folder where korge-fleks is located
#  - ../korge-fleks
```

## `settings.gradle.kts`

Needed settings for gradle to make kproject usable in the project.

```kotlin
pluginManagement { repositories { mavenLocal(); mavenCentral(); google(); gradlePluginPortal() } }

plugins {
  id("com.soywiz.kproject.settings") version "0.3.1"
}

kproject("./deps")
```

## `fleks/kproject.yml`

This is the kproject config for including Fleks sources into the KorGE project. Since `Entity` value objects
from Fleks need to be serializable for saving the game state the `serialization` plugin needs to be added.

```yaml
# loading git tag release (or commit) from GitHub repo (https://github.com/Quillraven/Fleks)
src: git::Quillraven/Fleks::/src::c24925091ced418bf045ba0672734addaab573d8
# or using Fleks sources locally in sub-folder "src"
#src: .
plugins:
  - serialization
```

## `korge-fleks/kproject.yml`

This is the kproject config for including KorGE-Fleks sources into the KorGE project. Also for KorGE-Fleks
the `serialization` plugin is needed to save the game state.

```yaml
plugins:
  - serialization
dependencies:
  - ../fleks
  - maven::common::com.soywiz.korlibs.korge2:korge
  # loading git tag release (or commit) from GitHub repo for KorGE-Parallax
  - https://github.com/korlibs/korge-parallax/tree/b8e7356c3c5ba5fac273a83d3f6ef127a16de739/korge-parallax
# or a local folder where korge-parallax is located
#  - ../../korge-parallax/korge-parallax
  # loading git tag release (or commit) from GitHub repo for KorGE-Tiled
  - https://github.com/korlibs/korge-tiled/tree/0.0.2/korge-tiled
# or a local folder where korge-tiled is located
#  - ../../korge-tiled/korge-tiled
```

## `korge-parallax/kproject.yml`

This is the kproject config for KorGE-Parallax sources. It basically contains only the dependency
of KorGE-Parallax to KorGE.

```yaml
dependencies:
  - maven::common::com.soywiz.korlibs.korge2:korge
```

There is also a kproject file for korge-tiled. It looks basically the same as that one for
korge-parallax and therefore is omitted here.

When changes are needed in one of the kproject libs above than it is possible to use a local copy of the
corresponding git repo in the `libs` folder. E.g. for KorGE-Parallax the `src:` line with git details can be
commented out and the `src:` line with local folder under `../../korge-parallax/korge-parallax` can be
uncommented.

# Updating KorGE-Fleks to newer versions

KorGE-Fleks depends on specific versions of KorGE, KorGE-Parallax addon, KorGE-Tiled addon and Fleks ECS.

The current versions which are working together can be seen at the top of this readme in section
"Supported Versions".

KorGE, Fleks ECS and all KorGE Addon versions need to be updated in following places:

## KorGE version

KorGE version needs to be updated in `gradle/libs.versions.toml`:

```kotlin
[plugins]
korge = { id = "com.soywiz.korge", version = "4.0.0" }
```

## Fleks version

Fleks ECS version needs to be updated in the kproject file under `fleks/kproject.yml`:

```
[...]
src: git::Quillraven/Fleks::/src::2.3
```

## KorGE Addon versions

All versions of additionally used KorGE addons (KorGE-Parallax, KorGE-Tiled) needs to be updated
in KorGE-Fleks kproject file under `korge-fleks/kproject.yml`:

```kotlin
[...]
dependencies:
[...]
- https://github.com/korlibs/korge-parallax/tree/0.0.3/korge-parallax
- https://github.com/korlibs/korge-tiled/tree/0.0.2/korge-tiled
```
# Examples

* [Example in this repo](https://github.com/korlibs/korge-fleks/tree/main/example)

  <img width="546" alt="Screenshot 2022-10-26 at 13 54 12" src="https://user-images.githubusercontent.com/570848/198019508-dafdb3a5-02af-49f7-92ec-9f76533c2524.png">

* [KorGE-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world)

# History

* <https://github.com/korlibs-archive/korge-next/pull/472>
* <https://github.com/korlibs/korge/pull/988>
