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
    var layerNames: List<String> = listOf(),

    var levelLayer: String = "",  // The level and layer name in the LDtk world

    var width: Float = 0f,  // Size of the level map
    var height: Float = 0f,

    // internal
    var initialized: Boolean = false
) : CloneableComponent<LdtkLevelMapComponent>() {
    override fun type(): ComponentType<LdtkLevelMapComponent> = LdtkLevelMapComponent

    // Get size of level map and save it into properties of this component
    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        val assetStore: AssetStore = this.inject(name = "AssetStore")
        val tileMapData = assetStore.getTileMapData(levelName, layerNames.first())
        width = tileMapData.data.width.toFloat()
        height = tileMapData.data.height.toFloat()
    }

    companion object : ComponentType<LdtkLevelMapComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LdtkLevelMapComponent = this.copy()
}
