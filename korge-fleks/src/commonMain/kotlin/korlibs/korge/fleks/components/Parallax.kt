package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.EntityConfig
import korlibs.korge.fleks.utils.SerializeBase
import korlibs.korge.fleks.utils.noConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is setting up the parallax background entity with its sub-entities.
 * It is used in the hook function for DrawableFamily.
 */
@Serializable
@SerialName("Parallax")
data class Parallax(
    var assetConfig: EntityConfig = noConfig
) : Component<Parallax>, SerializeBase {
    override fun type(): ComponentType<Parallax> = Parallax
    companion object : ComponentType<Parallax>()
}
