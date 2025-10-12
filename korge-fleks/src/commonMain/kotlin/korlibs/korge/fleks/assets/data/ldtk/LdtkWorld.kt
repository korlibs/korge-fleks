package korlibs.korge.fleks.assets.data.ldtk

import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.slice
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.image.tiles.TileSet
import korlibs.image.tiles.TileSetTileInfo
import korlibs.io.file.VfsFile
import korlibs.korge.ldtk.LDTKJson
import korlibs.korge.ldtk.LayerDefinition
import korlibs.korge.ldtk.LayerInstance
import korlibs.korge.ldtk.Level
import korlibs.korge.ldtk.TilesetDefinition
import korlibs.math.geom.RectangleInt


class ExtTileset(val def: TilesetDefinition, val tileset: TileSet)

class LdtkLayer(val level: LdtkLevel, val layer: LayerInstance) {
    val world get() = level.world
    val entities get() = layer.entityInstances
}

class LdtkLevel(val world: LdtkWorld, val level: Level) {
    val ldtk get() = world.ldtk
    val layers by lazy { level.layerInstances?.map { layer -> LdtkLayer(this@LdtkLevel, layer) } ?: emptyList() }
    val layersByName by lazy { layers.associateBy { it.layer.identifier } }
}


class LdtkWorld(
    val ldtk: LDTKJson,
    val tilesetDefsById: Map<Int, ExtTileset>
) {
    val levels by lazy { ldtk.levels.map { level -> LdtkLevel(this@LdtkWorld, level) } }
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


suspend fun VfsFile.readLdtkWorld(): LdtkWorld {
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
        val bitmap: Bitmap = def.relPath?.let { file.parent[it].readBitmap() } ?: error("Tileset image not found: ${def.relPath}")
        val tileCount = def.cWid * def.cHei

        println("Loading tileset: ${def.identifier}")
        val tileset = TileSet(
            (0 until tileCount).map { index ->
//                println("Tileset: ${def.identifier} Tile: $index / $tileCount")
                TileSetTileInfo(
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
        def.uid to ExtTileset(def, tileset)
    }
    return LdtkWorld(ldtk, tilesetDefsById)
}
