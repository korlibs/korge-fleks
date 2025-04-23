package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.CloneableComponent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Currently this is used to get the layout details for an Entity from the asset object.
 * The asset object is configured from GameModel.
 */
@Serializable @SerialName("Layout")
data class LayoutComponent(
    var centerX: Boolean = false,
    var centerY: Boolean = false,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
) : CloneableComponent<LayoutComponent>() {
    override fun type(): ComponentType<LayoutComponent> = LayoutComponent
    companion object : ComponentType<LayoutComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LayoutComponent = this.copy()
}
