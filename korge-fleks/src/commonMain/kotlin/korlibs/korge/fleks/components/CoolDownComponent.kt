package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable @SerialName("CoolDown")
data class CoolDown(
    val value: Float,
) : Poolable<CoolDown>() {
    override fun type() = CoolDownComponent


    companion object {
        val CoolDownComponent = componentTypeOf<CoolDown>()
    }

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun World.clone(): CoolDown =
        this.copy()
}
