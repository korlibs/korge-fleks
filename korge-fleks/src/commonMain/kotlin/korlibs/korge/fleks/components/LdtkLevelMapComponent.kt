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
    var width: Float = 0f,  // Size of the level map
    var height: Float = 0f
) : Component<LdtkLevelMapComponent> {
    override fun type(): ComponentType<LdtkLevelMapComponent> = LdtkLevelMapComponent

    // Get size of level map and save it into properties of this component
    override fun World.onAdd(entity: Entity) {
        val ldtkLevelMapComponent = entity[LdtkLevelMapComponent]
        val ldtkLevel = AssetStore.getLdtkLevel(AssetStore.getLdtkWorld(ldtkLevelMapComponent.worldName), ldtkLevelMapComponent.levelName)
        width = ldtkLevel.pxWid.toFloat()
        height = ldtkLevel.pxHei.toFloat()
    }

    companion object : ComponentType<LdtkLevelMapComponent>()
}
