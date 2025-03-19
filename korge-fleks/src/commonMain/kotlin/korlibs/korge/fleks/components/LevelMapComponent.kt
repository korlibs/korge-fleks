package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


@Serializable @SerialName("LevelMap")
data class LevelMapComponent(
    /**
     * The unique identifier for the level
     */
    var levelName: String = "",
    /**
     * List of layer names which shall be drawn by the specific render system.
     * Render order is specified by order of strings in the list.
     *
     * Example: ["Background", "Playfield", "Collisions"]
     */
    var layerNames: List<String> = listOf()
) : CloneableComponent<LevelMapComponent>() {
    override fun type(): ComponentType<LevelMapComponent> = LevelMapComponent
    companion object : ComponentType<LevelMapComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LevelMapComponent =
        LevelMapComponent(
            levelName = levelName,
            layerNames = layerNames.clone()
        )
}
