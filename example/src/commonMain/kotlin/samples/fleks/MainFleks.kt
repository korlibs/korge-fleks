package samples.fleks

import korlibs.korge.scene.Scene
import korlibs.korge.view.container
import korlibs.korge.view.addUpdater
import com.github.quillraven.fleks.*
import korlibs.korge.view.SContainer
import samples.fleks.assets.Assets
import samples.fleks.systems.*
import samples.fleks.components.*
import samples.fleks.entities.createMeteoriteSpawner

class MainFleksSample : Scene() {
    companion object {
        const val scaleFactor = 3
    }
    private val assets = Assets()

    override suspend fun SContainer.sceneInit() {

        // Configure and load the asset objects
        val config = Assets.Config(
            images = listOf(
                Pair("meteorite", "sprites.ase")
            )
        )
        assets.load(config)
    }

    override suspend fun SContainer.sceneMain() {
        container {
            scaleAvg = scaleFactor.toFloat()

            // Here are the container views which contain the generated entity objects with visible component "Sprite" attached to it
            //
            // TODO Build a more flexible views container system for handling layers for the SpriteSystem of Fleks ECS
            val layer0 = container()
            // val layer1 = container() // Add more layers when needed - This will be on top of layer0

            // This is the world object of the entity component system (ECS)
            // It contains all ECS related system and component configuration
            val world = world(entityCapacity = 512) {
                // Register external objects which are used by systems and component listeners
                injectables {
                    add(assets)  // Assets are used by the SpriteSystem / SpriteListener to get the image data for drawing
                    add("layer0", layer0)  // Currently, we use only one layer to draw all objects to - this is also used in SpriteListener to add the image to the layer container
                    // inject("layer1", layer1)  // Add more layers when needed e.g. for explosion objects to be on top, etc.
                }

                // Register component hooks which trigger actions when specific components are created
                components {
                    onAdd(Sprite, Sprite.onComponentAdded)
                    onRemove(Sprite, Sprite.onComponentRemoved)
                }

                // Register family hooks which trigger actions when specific entities (combination of components) are created
                families {
                    // not used here
                }

                // Register all needed systems of the entity component system
                // The order of systems here also define the order in which the systems are called inside Fleks ECS
                systems {
                    add(MoveSystem())
                    add(SpawnerSystem())
                    add(CollisionSystem())
                    add(DestructSystem())
                    add(SpriteSystem())   // Drawing images on screen should be last otherwise the position might be (0, 0) because it was not set before
                }
            }

            // Create an entity object which will spawn meteorites on top of the visual screen area
            world.createMeteoriteSpawner()

            // Run the update of the Fleks ECS - this will periodically call all update functions of the systems (e.g. onTick(), onTickEntity(), etc.)
            addUpdater { dt ->
                world.update(dt.seconds.toFloat())
            }
        }
    }
}
