package korlibs.korge.fleks.assets.data.ldtk

import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.tiles.TileSet
import korlibs.image.tiles.TileSetTileInfo
import korlibs.io.file.VfsFile
import korlibs.math.geom.RectangleInt


class ExtTileset(val def: TilesetDefinition, val tilesetName: String)

class LDTKLayer(val level: LDTKLevel, val layer: LayerInstance) {
    val world get() = level.world
    val entities get() = layer.entityInstances
}

class LDTKLevel(val world: LDTKWorld, val level: Level) {
    val ldtk get() = world.ldtk
    val layers by lazy { level.layerInstances?.map { layer -> LDTKLayer(this@LDTKLevel, layer) } ?: emptyList() }
    val layersByName by lazy { layers.associateBy { it.layer.identifier } }
}


class LDTKWorld(
    val ldtk: LDTKJson,
    val tilesetDefsById: Map<Int, ExtTileset>
) {
    val levels by lazy { ldtk.levels.map { level -> LDTKLevel(this@LDTKWorld, level) } }
    val levelsByName by lazy { levels.associateBy { it.level.identifier } }

    val layersDefsById: Map<Int, LayerDefinition> = ldtk.defs.layers.associateBy { it.uid }

    val colors = Bitmap32((ldtk.defaultGridSize + 4) * 16, ldtk.defaultGridSize)
    val intsTileSet = TileSet(
        (0 until 16).map {
            TileSetTileInfo(
                it,
                colors.slice(
                    RectangleInt(
                        (ldtk.defaultGridSize + 4) * it,
                        0,
                        ldtk.defaultGridSize,
                        ldtk.defaultGridSize
                    )
                )
            )
        }
    )

    init {
        // @TODO: Do this for each layer, since we might have several IntGrid layers
        for (layer in ldtk.defs.layers) {
            for (value in layer.intGridValues) {
                colors.fill(Colors[value.color], (ldtk.defaultGridSize + 4) * value.value)
                //println("COLOR: ${value.value} : ${value.color}")
            }
        }
    }
}


suspend fun VfsFile.readLdtkWorld(): LDTKWorld {
    val file = this
    val json = file.readString()
    val ldtk = LDTKJson.load(json)

    // Load texture bitmaps of tiles which are already extruded and build up a TileSet object
    println("Tilesets in LDtk file: (${file.absolutePath})")
    val tilesetDefsById: Map<Int, ExtTileset> = ldtk.defs.tilesets.mapNotNull { tilesetConfig ->
        // Leave out tilesets which do not have an image (e.g. LDtk internal tileset icons)
        if (tilesetConfig.relPath == null) null else tilesetConfig
    }.associate { tilesetDef ->
        val tilesetName = tilesetDef.identifier
        println("- $tilesetName")
        tilesetDef.uid to ExtTileset(tilesetDef, tilesetName)
    }
    return LDTKWorld(ldtk, tilesetDefsById)
}
