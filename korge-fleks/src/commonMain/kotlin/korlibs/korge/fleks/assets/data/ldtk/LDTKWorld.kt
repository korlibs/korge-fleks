package korlibs.korge.fleks.assets.data.ldtk

import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.tiles.TileSet
import korlibs.image.tiles.TileSetTileInfo
import korlibs.io.file.VfsFile
import korlibs.math.geom.RectangleInt


class ExtTileset(val def: TilesetDefinition, val tileset: TileSet)

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

    //val ldtk = world.ldtk
    //val layersDefsById = world.layersDefsById
    //val tilesetDefsById = world.tilesetDefsById

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


suspend fun VfsFile.readLdtkWorld(
    // callback to get a tileset object - it will be already filled with tiles from texture atlas on loading the atlas
    callback: ((String, Int) -> TileSet)
): LDTKWorld {
    val file = this
    val json = file.readString()
    val ldtk = LDTKJson.load(json)

    ldtk.defs.tilesets.forEach { def ->
    }

    // Load texture bitmaps of tiles which are already extruded and build up a TileSet object
    val tilesetDefsById: Map<Int, ExtTileset> = ldtk.defs.tilesets.mapNotNull {
        // Leave out tilesets which do not have an image (e.g. LDtk internal tileset icons)
        if (it.relPath == null) null else it
    }.associate { def ->
//        val bitmap: Bitmap = def.relPath?.let { file.parent[it].readBitmap() } ?: error("Tileset image not found: ${def.relPath}")
        val tileCount = def.cWid * def.cHei
        val tilesetName = def.identifier
        val tileset = callback(tilesetName, tileCount)
/*
        // TODO: This tile set could contain slices for tiles which are located in a texture atlas
        //       tile id would be the name of the tile in that case (the number part only probably)
        //       but for now we only support single image tilesets
        val tileset = TileSet(
            (0 until tileCount).map { index ->
//                println("Tileset: ${def.identifier} Tile: $index / $tileCount")
                TileSetTileInfo(
                    // This is the tile id which is used in TileMapData to reference this tile
                    index,
                    bitmap.slice(
                        RectangleInt(
                            x = (index % def.cWid) * (def.tileGridSize + 2) + 1,
                            y = (index / def.cWid) * (def.tileGridSize + 2) + 1,
                            def.tileGridSize,
                            def.tileGridSize
                        )
                    )
                )
            }
        )
*/
//        println()
        def.uid to ExtTileset(def, tileset)
    }
//    println()
    return LDTKWorld(ldtk, tilesetDefsById)
}
