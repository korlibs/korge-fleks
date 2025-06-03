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
class NinePatch private constructor(
    var name: String = "",
    var width: Float = 0f,
    var height: Float = 0f
) : PoolableComponents<NinePatch>() {
    override fun type() = NinePatchComponent

    companion object {
        val NinePatchComponent = componentTypeOf<NinePatch>()

        // Use this function to create a new instance as val inside another component
        fun staticNinePatchSpriteComponent(config: NinePatch.() -> Unit ): NinePatch =
            NinePatch().apply(config)

        // Use this function to get a new instance from the pool
        fun World.NinePatchComponent(config: NinePatch.() -> Unit ): NinePatch =
        getPoolable(NinePatchComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addNinePatchComponentPool(preAllocate: Int = 0) {
            addPool(NinePatchComponent, preAllocate) { NinePatch() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): NinePatch =
    getPoolable(NinePatchComponent).apply { init(from = this@NinePatch ) }

    // Init an existing component instance with data from another component
    fun init(from: NinePatch) {
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
