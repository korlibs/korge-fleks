package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to ...
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("SpriteLayer")
class SpriteLayer private constructor(
    var offsetX: Float = 0f,
    var offsetY: Float = 0f,
    @Serializable(with = RGBAAsInt::class) var rgba: RGBA = Colors.WHITE
) : Poolable<SpriteLayer>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: SpriteLayer) {
        offsetX = from.offsetX
        offsetY = from.offsetY
        rgba = from.rgba.cloneRgba()
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        offsetX = 0f
        offsetY = 0f
        rgba = Colors.WHITE
    }

    override fun type() = SpriteLayerComponent

    companion object {
        val SpriteLayerComponent = componentTypeOf<SpriteLayer>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticSpriteLayerComponent(config: SpriteLayer.() -> Unit ): SpriteLayer =
            SpriteLayer().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.SpriteLayerComponent(config: SpriteLayer.() -> Unit ): SpriteLayer =
        getPoolable(SpriteLayerComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addSpriteLayerDataPool(preAllocate: Int = 0) {
            addPool(SpriteLayerComponent, preAllocate) { SpriteLayer() }
        }

        // Init and cleanup functions for collections of SpriteLayer
        fun MutableMap<String, SpriteLayer>.init(world: World, from: Map<String, SpriteLayer>) {
            from.forEach { (key, item) ->
                this[key] = item.run { world.clone() }
            }
        }

        fun MutableMap<String, SpriteLayer>.cleanup(world: World) {
            this.forEach { (_, item) ->
                item.cleanup()
                item.run { world.free() }
            }
            this.clear()
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): SpriteLayer =
        getPoolable(SpriteLayerComponent).apply { init(from = this@SpriteLayer ) }

    // Cleanup/Reset the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // true (alpha > 0) - false (alpha == 0)
    var visibility: Boolean = rgba.a != 0
        get() = rgba.a != 0
        set(value) {
            rgba = if (value) rgba.withAf(1f) else rgba.withAf(0f)
            field = value
        }

}
