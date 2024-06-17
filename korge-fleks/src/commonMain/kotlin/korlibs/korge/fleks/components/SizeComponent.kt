package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

/**
 * This component is used to add a size to a game object.
 */
@Serializable @SerialName("Size")
data class SizeComponent(
    var width: Float = 0f,
    var height: Float = 0f,
) : CloneableComponent<SizeComponent>() {
    override fun type(): ComponentType<SizeComponent> = SizeComponent
    companion object : ComponentType<SizeComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): SizeComponent = this.copy()
}
