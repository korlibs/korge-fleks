package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to store the cooldown time/value of a game object.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("CoolDown")
class CoolDown private constructor(
    var value: Float = 0f
) : Poolable<CoolDown>() {
    override fun type() = CoolDownComponent

    companion object {
        val CoolDownComponent = componentTypeOf<CoolDown>()

        fun World.CoolDownComponent(config: CoolDown.() -> Unit ): CoolDown =
            getPoolable(CoolDownComponent).apply { config() }

        fun InjectableConfiguration.addCoolDownComponentPool(preAllocate: Int = 0) {
            addPool(CoolDownComponent, preAllocate) { CoolDown() }
        }
    }

    override fun World.clone(): CoolDown =
        getPoolable(CoolDownComponent).apply {
            value = this@CoolDown.value
        }
}
