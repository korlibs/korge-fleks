package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.assetmanager.*
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
    var levelName: String = "",
    var layerName: String? = null,  // TODO enable LDtkLevelView to show only defined layers
    var width: Float = 0f,  // Size of the level map
    var height: Float = 0f,

    // internal
    var initialized: Boolean = false
) : Component<LdtkLevelMapComponent> {
    override fun type(): ComponentType<LdtkLevelMapComponent> = LdtkLevelMapComponent

    // Get size of level map and save it into properties of this component
    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        val assetStore: AssetStore = this.inject(name = "AssetStore")

        val ldtkLevelMapComponent = entity[LdtkLevelMapComponent]
        val ldtkLevel = assetStore.getLdtkLevel(assetStore.getLdtkWorld(ldtkLevelMapComponent.worldName), ldtkLevelMapComponent.levelName)
        width = ldtkLevel.pxWid.toFloat()
        height = ldtkLevel.pxHei.toFloat()
    }

    companion object : ComponentType<LdtkLevelMapComponent>()

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : LdtkLevelMapComponent = this.copy()
}
