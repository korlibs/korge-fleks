package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba
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
    val position: Position = staticPositionComponent {},
    val rgba: Rgba = staticRgbaComponent {}
) : Poolable<TextureRef> {
    // Init an existing tween data instance with data from another tween
    override fun init(from: TextureRef) {
        name = from.name
        entity = from.entity
        position.init(from.position)
        rgba.init(from.rgba)
    }

    override fun cleanup() {
        name = ""
        entity = Entity.NONE
        position.cleanup()
        rgba.cleanup()
    }

    override fun clone(): TextureRef = pool.alloc().apply { init(from = this@TextureRef) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as val inside a component
        fun staticTextureRef(config: TextureRef.() -> Unit ): TextureRef =
            TextureRef().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun textureRef(config: TextureRef.() -> Unit ): TextureRef =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TextureRef") { TextureRef() }

        // Init and cleanup functions for collections of TextureRef
        fun MutableList<TextureRef>.init(from: List<TextureRef>) {
            from.forEach { item ->
                this.add(item.clone())
            }
        }

        fun MutableList<TextureRef>.free() {
            this.forEach { item ->
                item.cleanup()
                item.free()
            }
            this.clear()
        }

        fun MutableMap<String, TextureRef>.init(from: Map<String, TextureRef>) {
            from.forEach { (key, item) ->
                this[key] = item.clone()
            }
        }

        fun MutableMap<String, TextureRef>.free() {
            this.forEach { (_, item) ->
                item.cleanup()
                item.free()
            }
            this.clear()
        }
    }
}
