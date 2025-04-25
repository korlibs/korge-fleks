package korlibs.korge.fleks.components


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable @SerialName("Debug")
data class DebugComponent(
    val name: String = ""
) : Poolable<DebugComponent>() {
    override fun type() = DebugComponent

    companion object : ComponentType<DebugComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): DebugComponent =
        this.copy()
}
