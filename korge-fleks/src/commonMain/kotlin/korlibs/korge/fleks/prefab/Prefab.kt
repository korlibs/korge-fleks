package korlibs.korge.fleks.prefab

import korlibs.image.tiles.TileMapData
import korlibs.korge.fleks.prefab.data.LevelData
import kotlin.native.concurrent.ThreadLocal


@ThreadLocal
object Prefab {
    // This is loaded in the AssetStore as part of LEVEL data assets
    var levelName: String = ""  // name of the current level - needed for hot-reloading of level map data from LDtk tile map
    var levelData: LevelData? = null  // levels with chunk data
}