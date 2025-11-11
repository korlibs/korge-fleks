package korlibs.korge.fleks.assets.data

import korlibs.image.tiles.TileMapData
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.assets.data.ldtk.LDTKWorld
import korlibs.korge.fleks.assets.data.ldtk.Level

/**
 * This class contains the tile map data for each chunk of the level.
 */
class LevelMapConfig(
    private val assetStore: AssetStore,
    private var name: String = "noName",
    ldtkWorld: LDTKWorld,
    ldtkLevel: Level
) {
    private val tileMapStack: MutableMap<String, TileMapData> = mutableMapOf()

    /**
     * Width of the level in pixels.
     */
    var levelWidth: Int = 0
        private set

    /**
     * Height of the level in pixels.
     */
    var levelHeight: Int = 0
        private set

    init {
        // Save TileMapData for each layer from the LDtk level
        ldtkLevel.layerInstances?.forEach { ldtkLayer ->
            // Check if layer has tile set -> store tile map data for layer
            val tilesetExt = ldtkWorld.tilesetDefsById[ldtkLayer.tilesetDefUid]
            if (tilesetExt != null) {
                addLDtkLayer()
// TODO               layerTileMaps[ldtkLayer.identifier] = createTileMapData(ldtkLayer, tilesetExt)
                levelWidth = ldtkLayer.cWid * tilesetExt.def.tileGridSize
                levelHeight = ldtkLayer.cHei * tilesetExt.def.tileGridSize
            }
        }
    }

    private fun addLDtkLayer() {
        // TODO implement
    }
}