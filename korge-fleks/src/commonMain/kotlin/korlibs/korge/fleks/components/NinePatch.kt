package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to define a NinePatch texture for an entity.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("NinePatchSprite")
class NinePatchSprite private constructor(
    var name: String = "",
    var width: Float = 0f,
    var height: Float = 0f
) : Poolable<NinePatchSprite>() {
    override fun type() = NinePatchSpriteComponent

    companion object {
        val NinePatchSpriteComponent = componentTypeOf<NinePatchSprite>()

        // Use this function to create a new instance as val inside another component
        fun staticNinePatchSpriteComponent(config: NinePatchSprite.() -> Unit ): NinePatchSprite =
            NinePatchSprite().apply(config)

        // Use this function to get a new instance from the pool
        fun World.NinePatchSpriteComponent(config: NinePatchSprite.() -> Unit ): NinePatchSprite =
        getPoolable(NinePatchSpriteComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addNinePatchSpriteComponentPool(preAllocate: Int = 0) {
            addPool(NinePatchSpriteComponent, preAllocate) { NinePatchSprite() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): NinePatchSprite =
    getPoolable(NinePatchSpriteComponent).apply { init(from = this@NinePatchSprite ) }

    // Init an existing component instance with data from another component
    fun init(from: NinePatchSprite) {
        name = from.name
        width = from.width
        height = from.height
    }

    // Cleanup the component instance manually
    fun cleanup() {
        name = ""
        width = 0f
        height = 0f
    }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup the component automatically when it is removed from an entity
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
