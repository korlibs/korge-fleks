package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.CloneableComponent
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

/**
 * This component is used to add a size (as integer numbers) to a game object.
 */
@Serializable @SerialName("SizeInt")
data class SizeIntComponent(
    var width: Int = 0,
    var height: Int = 0,
) : CloneableComponent<SizeIntComponent>() {
    override fun type(): ComponentType<SizeIntComponent> = SizeIntComponent
    companion object : ComponentType<SizeIntComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): SizeIntComponent = this.copy()
}
