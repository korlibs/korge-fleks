package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable @SerialName("CoolDown")
data class CoolDownComponent(
    val value: Float,
) : PoolableComponent1<CoolDownComponent>() {
    override fun type() = CoolDownComponent

    companion object : ComponentType<CoolDownComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): CoolDownComponent =
        this.copy()
}
