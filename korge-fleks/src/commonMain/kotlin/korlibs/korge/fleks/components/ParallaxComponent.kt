package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.entity.config.nothing
import korlibs.korge.fleks.utils.Identifier
import korlibs.korge.fleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is setting up the parallax background entity with its sub-entities.
 * It is used in the hook function for DrawableFamily.
 */
@Serializable
@SerialName("Parallax")
data class ParallaxComponent(
    var config: Identifier = nothing
) : Component<ParallaxComponent> {
    override fun type(): ComponentType<ParallaxComponent> = ParallaxComponent
    companion object : ComponentType<ParallaxComponent>()
}
