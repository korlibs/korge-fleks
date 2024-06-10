package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*

/**
 * This component is used to add a size to a game object.
 */
@Serializable
@SerialName("Size")
data class SizeComponent(
    var width: Float = 0f,
    var height: Float = 0f,
) : Component<SizeComponent> {
    override fun type(): ComponentType<SizeComponent> = SizeComponent
    companion object : ComponentType<SizeComponent>()

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : SizeComponent = this.copy()
}
