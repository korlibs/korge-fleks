package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


@Serializable @SerialName("LdtkLevelMap")
data class LdtkLevelMapComponent(
    /**
     * The unique identifier (level name) of the level from the LDtk world
     */
    var levelName: String = "",
    /**
     * List of layer names which shall be drawn by the specific render system.
     *
     * Example: ["Background", "Playfield"]
     */
    var layerNames: List<String> = listOf()
) : CloneableComponent<LdtkLevelMapComponent>() {
    override fun type(): ComponentType<LdtkLevelMapComponent> = LdtkLevelMapComponent
    companion object : ComponentType<LdtkLevelMapComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LdtkLevelMapComponent =
        LdtkLevelMapComponent(
            levelName = levelName,
            layerNames = layerNames.clone()
        )
}
