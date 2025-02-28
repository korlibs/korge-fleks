# KorGE-Fleks

This is the [Fleks Entity Components System](https://github.com/Quillraven/Fleks) (ECS) integration for KorGE Game Engine.

KorGE-Fleks is not just a simple wrapper around Fleks ECS but a specialized implementation containing a lot additional
usefull stuff around an ECS for the KorGE Game Engine. Korge-fleks is dedicated to 2D platform games. It is developed in
the open with the hope that it will be useful for others, too.

Together with the [korge-fleks-hello-world](https://github.com/korlibs/korge-fleks-hello-world) example it
can be used as a starting point for a new (2D platformer) game project.

Upstream project for Fleks ECS can be found here: <https://github.com/Quillraven/Fleks>

KorGE-Fleks is maintained by [@jobe-m](https://github.com/jobe-m)

# Features

Korge-fleks offers the following features for building a 2D platform game:

## Game state management
- Entity Component System (ECS) utilizing Fleks
- Serialization of Entities and Components
- Snapshot system for saving and restoring the game state
- Save and load game state (world snapshot) as JSON string

## Asset management
- Asset store for loading and managing game assets
- Hot-reloading of assets

## Game Object configuration
- Entity configuration objects for easy setup of complex Game Objects

## Rendering systems
- Game object renderer for sprites, animations, text, 9-patches, etc.
- Parallax background renderer
- Level map renderer (LDtk level map format)
- Fast objects/effects renderer (e.g. explosions)
- Debug rectangle renderer (texture bounds, collision shapes, etc.)

## Level map management
- Support for building huge worlds of seamlessly connected levels from LDtk

## Property animation system
- Tween-Animation system with Easing functions to animate properties of components
- Tween-Sequence system to create scripted animations (e.g. for intro or cut scenes)

## Sound system
- Sound system for playing background music and triggering sound effects from entities


Planed features:

- Collision detection system for level maps and game objects
- Pooling of Components


# Supported Versions

This is a list of versions for all needed modules/addons which are known to work together with
KorGE-Fleks:

```
- KorGE:                v6.0.0-beta4
- KorGE-LDtk addon:     v1.0.3
- Fleks:                2.9
```

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
- Identifier (String value class for accessing static entity configuration which is loaded from AssetStore and
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

## Entity Configuration

KorGE-Fleks contains some generic configuration assets which can be used to build up more complex game objects.
The existing config objects can be found in `korlibs.korge.fleks.entity.config`.
These object classes contain 4 sections:

- Config data classes
- Identifiers for invokable functions
- The actual (anonymous) configure function as a value object
- Init block which registers the configure function and its Identifier to the Invokable object store

A configuration for an entity is applied to the entity's components by executing
the configure function. The Invokable object stores the relationship between the Identifier and the function.
Systems like the SpawnerSystem will invoke the function by looking up the Identifier and executing invoke from
the Invokable object class. This is how KorGE-Fleks can set up new game objects/entities in a very generic and
configurable way. 

## AssetStore

...

### Hot-reloading of Assets

...

## Fleks World Integration into a KorGE Scene

...

# Set up a new Game with KorGE-Fleks

As a clean start the [KorGE-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world) repository can be used.
It contains the kproject and gradle setup to use Fleks, KorGE and KorGE addons _KorGE-Fleks and KorGE-LDtk in a project.

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
    - https://github.com/korlibs/korge-fleks/tree/0acbfcb5d89eca161c3537a9160143c9a72d2725/korge-fleks
#    or use local copy
#    - ./submodules/korge-fleks/korge-fleks
```

## `settings.gradle.kts`

Needed settings for gradle to make kproject usable in the project.

```kotlin
pluginManagement {
    repositories { mavenLocal(); mavenCentral(); google(); gradlePluginPortal() }
}

buildscript {
    val libsTomlFile = File(this.sourceFile?.parentFile, "gradle/libs.versions.toml").readText()
    var plugins = false
    var version = ""
    for (line in libsTomlFile.lines().map { it.trim() }) {
        if (line.startsWith("#")) continue
        if (line.startsWith("[plugins]")) plugins = true
        if (plugins && line.startsWith("korge") && Regex("^korge\\s*=.*").containsMatchIn(line)) version = Regex("version\\s*=\\s*\"(.*?)\"").find(line)?.groupValues?.get(1) ?: error("Can't find korge version")
    }
    if (version.isEmpty()) error("Can't find korge version in $libsTomlFile")

    repositories { mavenLocal(); mavenCentral(); google(); gradlePluginPortal() }

    dependencies {
        classpath("com.soywiz.korge.settings:com.soywiz.korge.settings.gradle.plugin:$version")
    }
}

apply(plugin = "com.soywiz.korge.settings")
```

## `fleks/kproject.yml`

This is the kproject config for including Fleks sources into the KorGE project. Since `Entity` value objects
from Fleks need to be serializable for saving the game state the `serialization` plugin needs to be added.

```yaml
# Get an external source project from GitHub which does not contain a kproject.yml file
# loading git tag release (or commit) from GitHub repo (https://github.com/Quillraven/Fleks)
src: git::Quillraven/Fleks::/src::2.9

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
# Or use fleks locally
#  - ../../fleks
#
    - maven::common::com.soywiz.korlibs.korge2:korge
    - https://github.com/korlibs/korge-ldtk/tree/v1.0.3/korge-ldtk
# Use local copy of KorGE addons
#    - ../../korge-ldtk/korge-ldtk
```

## `korge-ldtk/kproject.yml`

This is the kproject config for KorGE-LDtk sources. It basically contains only the dependency
of KorGE-LDtk to KorGE.

```yaml
name: "korge-ldtk"
plugins:
    - serialization
dependencies:
    - "maven::common::com.soywiz.korlibs.korge2:korge"
```

There is also a kproject files for the other KorGE-addons. They look basically the same as that one for
`korge-ldtk` and therefore is omitted here.

When changes are needed in one of the kproject libs above than it is possible to use a local copy of the
corresponding git repo in the `modules` folder. E.g. for KorGE-LDtk the `src:` line with git details can be
commented out and the `src:` line with local folder under `../../korge-ldtk/korge-ldtk` can be
uncommented.

# Updating KorGE-Fleks to newer versions

KorGE-Fleks depends on specific versions of KorGE, KorGE-Ldtk addon and Fleks ECS.

The current versions which are working together can be seen at the top of this readme in section
"Supported Versions".

KorGE, Fleks ECS and all KorGE Addon versions need to be updated in following places:

## KorGE version

KorGE version needs to be updated in `gradle/libs.versions.toml`:

```toml
[plugins]
korge = { id = "com.soywiz.korge", version = "5.4.0" }
```

## Fleks version

Fleks ECS version needs to be updated in the kproject file under `fleks/kproject.yml`:

```yaml
[...]
src: git::Quillraven/Fleks::/src::2.x
```

## KorGE Addon versions

All versions of additionally used KorGE addons (KorGE-LDtk) needs to be updated
in KorGE-Fleks kproject file under `korge-fleks/kproject.yml`:

```yaml
[...]
dependencies:
[...]
- https://github.com/korlibs/korge-ldtk/tree/v1.0.x/korge-ldtk
```

# Examples

* [Korge-fleks-hello-world example](https://github.com/korlibs/korge-fleks-hello-world)

  <img width="546" alt="Screenshot 2022-10-26 at 13 54 12" src="https://user-images.githubusercontent.com/570848/198019508-dafdb3a5-02af-49f7-92ec-9f76533c2524.png">

* [KorGE-Fleks Hello World](https://github.com/korlibs/korge-fleks-hello-world)

# History

* <https://github.com/korlibs-archive/korge-next/pull/472>
* <https://github.com/korlibs/korge/pull/988>
