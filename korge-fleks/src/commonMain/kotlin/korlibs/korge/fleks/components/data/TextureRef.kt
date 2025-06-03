package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.staticRgbaComponent
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This class is used to reference a texture layer in a sprite and being able to
 * animate its position and color properties with the [TweenSequenceSystem].
 */
@Serializable @SerialName("TextureRef")
class TextureRef private constructor(
    var name: String = "",
    var entity: Entity = Entity.NONE,  // Link to entity for tween animation
    /**
     * Local position of layer relative to the top-left point of the parallax entity (global PositionComponent).
     */
    val position: Position = staticPositionComponent(),
    val rgba: Rgba = staticRgbaComponent()
) : PoolableComponents<TextureRef>() {
    override fun type() = TextureRefData

    companion object {
        val TextureRefData = componentTypeOf<TextureRef>()

        // Use this function to create a new instance as val inside another component
        fun staticTextureRefData(config: TextureRef.() -> Unit ): TextureRef =
            TextureRef().apply(config)

        // Use this function to get a new instance from the pool
        fun World.TextureRefData(config: TextureRef.() -> Unit ): TextureRef =
            getPoolable(TextureRefData).apply(config)

        fun InjectableConfiguration.addTextureRefDataPool(preAllocate: Int = 0) {
            addPool(TextureRefData, preAllocate) { TextureRef() }
        }

        // Init and cleanup functions for collections of TextureRef
        fun MutableList<TextureRef>.init(world: World, from: List<TextureRef>) {
            from.forEach { item ->
                this.add(item.run { world.clone() } )
            }
        }

        fun MutableList<TextureRef>.cleanup(world: World) {
            this.forEach { item ->
                item.cleanup()
                item.run { world.free() }
            }
            this.clear()
        }

        fun MutableMap<String, TextureRef>.init(world: World, from: Map<String, TextureRef>) {
            from.forEach { (key, item) ->
                this[key] = item.run { world.clone() }
            }
        }

        fun MutableMap<String, TextureRef>.cleanup(world: World) {
            this.forEach { (_, item) ->
                item.cleanup()
                item.run { world.free() }
            }
            this.clear()
        }
    }

    override fun World.clone(): TextureRef =
        getPoolable(TextureRefData).apply { init(from = this@TextureRef) }

    fun init(from: TextureRef) {
        name = from.name
        entity = from.entity
        position.init(from.position)
        rgba.init(from.rgba)
    }

    fun cleanup() {
        name = ""
        entity = Entity.NONE
        position.cleanup()
        rgba.cleanup()
    }
}
