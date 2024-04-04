package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import kotlinx.serialization.*


@Serializable
@SerialName("LdtkLevelMap")
data class LdtkLevelMapComponent(
    /**
     * The name of the LDtk world in the Asset manager
     */
    var worldName: String = "",
    /**
     * The unique identifier (level name) of the level from the LDtk world
     */
    var levelName: String = ""

) : Component<LdtkLevelMapComponent> {
    override fun type(): ComponentType<LdtkLevelMapComponent> = LdtkLevelMapComponent
    companion object : ComponentType<LdtkLevelMapComponent>()
}
