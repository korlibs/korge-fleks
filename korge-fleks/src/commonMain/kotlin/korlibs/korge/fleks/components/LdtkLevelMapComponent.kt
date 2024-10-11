package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


@Serializable @SerialName("LdtkLevelMap")
data class LdtkLevelMapComponent(
    /**
     * The name of the LDtk world in the Asset manager
     */
    var worldName: String = "",
    /**
     * The unique identifier (level name) of the level from the LDtk world
     */
    var levelName: String = "",
    /**
     * Optional: List of layer names which shall be drawn by the specific render system.
     * If not set, all layers will be drawn.
     *
     * Example: ["Background", "Playfield"]
     */
    var layerNames: List<String>? = null,

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

        val ldtkLevel = assetStore.getLdtkLevel(assetStore.getLdtkWorld(worldName), levelName)
        width = ldtkLevel.pxWid.toFloat()
        height = ldtkLevel.pxHei.toFloat()
    }

    companion object : ComponentType<LdtkLevelMapComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LdtkLevelMapComponent = this.copy()
}
