package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add debug features to a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Debug")
class Debug private constructor(
    var name: String = ""
) : Poolable<Debug>() {
    override fun type() = DebugComponent

    companion object {
        val DebugComponent = componentTypeOf<Debug>()

        fun World.DebugComponent(config: Debug.() -> Unit ): Debug =
            getPoolable(DebugComponent).apply { config() }

        fun InjectableConfiguration.addDebugComponentPool(preAllocate: Int = 0) {
            addPool(DebugComponent, preAllocate) { Debug() }
        }
    }

    override fun World.clone(): Debug =
        getPoolable(DebugComponent).apply {
            name = this@Debug.name
        }
}
