package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.SpriteLayer
import korlibs.korge.fleks.components.data.SpriteLayer.Companion.cleanup
import korlibs.korge.fleks.components.data.SpriteLayer.Companion.init
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component enables controlling of layer properties of a sprite texture.
 *
 * @param [layerMap] contains offset and rgba properies for specific layers of a sprite texture.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("SpriteLayers")
class SpriteLayers private constructor(
    val layerMap: MutableMap<String, SpriteLayer> = mutableMapOf()
) : PoolableComponents<SpriteLayers>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun World.init(from: SpriteLayers) {
        layerMap.init(world = this, from.layerMap)
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun World.cleanup() {
        layerMap.cleanup(world = this)
    }

    override fun type() = SpriteLayersComponent

    companion object {
        val SpriteLayersComponent = componentTypeOf<SpriteLayers>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSpriteLayersComponent(config: SpriteLayers.() -> Unit ): SpriteLayers =
            SpriteLayers().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.SpriteLayersComponent(config: SpriteLayers.() -> Unit ): SpriteLayers =
        getPoolable(SpriteLayersComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addSpriteLayersComponentPool(preAllocate: Int = 0) {
            addPool(SpriteLayersComponent, preAllocate) { SpriteLayers() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): SpriteLayers =
    getPoolable(SpriteLayersComponent).apply { init(from = this@SpriteLayers ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }
}
