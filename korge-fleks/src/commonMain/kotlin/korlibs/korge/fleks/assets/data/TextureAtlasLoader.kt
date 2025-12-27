package korlibs.korge.fleks.assets.data

import korlibs.datastructure.associateByInt
import korlibs.datastructure.toIntMap
import korlibs.image.atlas.Atlas
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.NinePatchBmpSlice
import korlibs.image.bitmap.slice
import korlibs.image.font.BitmapFont
import korlibs.image.format.ImageDecodingProps
import korlibs.image.format.readBitmapSlice
import korlibs.image.tiles.TileSet
import korlibs.image.tiles.TileSetTileInfo
import korlibs.io.dynamic.dyn
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.io.util.unquote
import korlibs.korge.fleks.assets.AssetModel.TextureConfig
import korlibs.korge.fleks.assets.BitMapFontMapType
import korlibs.korge.fleks.assets.NinePatchBmpSliceMapType
import korlibs.korge.fleks.assets.ParallaxLayersMapType
import korlibs.korge.fleks.assets.SpriteFramesMapType
import korlibs.korge.fleks.assets.TilesetMapType
import korlibs.korge.fleks.assets.TilesetMapType2
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.ParallaxPlane.*
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.ParallaxLayer
import korlibs.korge.fleks.assets.data.SpriteFrames.*
import korlibs.math.geom.RectangleInt
import korlibs.math.geom.slice.RectSlice
import kotlinx.serialization.json.Json
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.math.absoluteValue


/**
 * Iterate through all atlas entries and execute the given action for each entry
 * that starts with the given prefix. The action is provided with the entry and
 * the frameTag (entry name without prefix and animation index).
 */
inline fun Iterable<Atlas.Entry>.forEachWith(prefix: String, action: (Atlas.Entry, String) -> Unit) {
    val imagePrefixRegex = "^${prefix}".toRegex()
    val frameTagRegex = "_\\d+$".toRegex()
    for (entry in this) {
        if (imagePrefixRegex.containsMatchIn(entry.name)) {
            val imageName = entry.name.replace(imagePrefixRegex, "")
            val frameTag = if (frameTagRegex.containsMatchIn(imageName)) {
                imageName.replace(frameTagRegex, "")
            } else imageName
            action(entry, frameTag)
        }
    }
}

inline fun Iterable<Atlas.Entry>.forEach (action: (Atlas.Entry, String) -> Unit) {
    val frameTagRegex = "_\\d+$".toRegex()
    for (entry in this) {
        val imageName = entry.name
        val frameTag = if (frameTagRegex.containsMatchIn(imageName)) {
            imageName.replace(frameTagRegex, "")
        } else imageName
        action(entry, frameTag)
    }
}

suspend fun VfsFile.readKorgeFleksAssets(
    type: AssetType,
    textures: SpriteFramesMapType,
    ninePatchSlices: NinePatchBmpSliceMapType,
    bitMapFonts: BitMapFontMapType,
    parallaxLayers: ParallaxLayersMapType,
    tilesets: TilesetMapType2
) {
    // Enable ignoreUnknownKeys for testing when needed
    //val assetConfig: AssetConfig = Json { ignoreUnknownKeys = true }.decodeFromString(this.readString())
    val assetConfig: AssetConfig = Json.decodeFromString(this.readString())

    // Get version info
    val major: Int = assetConfig.version[0]
    val minor: Int = assetConfig.version[1]
    val build: Int = assetConfig.version[2]
    // Check later if asset version/build is compatible otherwise convert to new version

    val textureAtlases: List<BmpSlice> = assetConfig.textures.map { texture ->
        parent[texture].readBitmapSlice(props = ImageDecodingProps.DEFAULT)
    }

    // Load images and store into textures map
    assetConfig.images.forEach { (name, image) ->
        val frames = image.frames.map { frames ->
            val frame = frames.frame
            val index = frame[0]
            val x = frame[1]
            val y = frame[2]
            val width = frame[3]
            val height = frame[4]
            SpriteFrame(
                bmpSlice = textureAtlases.getOrElse(index) { error("readKorgeFleksAssets - texture atlas index '$index' for image '$name' not found!") }
                    .slice(RectangleInt(x, y, width, height)),
                targetX = frames.xOffset,
                targetY = frames.yOffset,
                duration = frames.duration.toFloat() / 1000f
            )
        }

        textures[name] = Pair(
            type, SpriteFrames(
                frames = frames.toMutableList(),
                width = image.width,
                height = image.height
            )
        )
    }

    // Load nine-patch images and store into ninePatchSlices map
    assetConfig.ninePatches.forEach { (name, image) ->
        ninePatchSlices[name] = Pair(
            type, NinePatchBmpSlice.createSimple(
                bmp = textureAtlases.getOrElse(
                    index = image.frame[0]) { error("readKorgeFleksAssets - texture atlas index '${image.frame[0]}' for nine-patch image '$name' not found!") }
                    .slice(RectangleInt(
                        x = image.frame[1],
                        y = image.frame[2],
                        width = image.frame[3],
                        height = image.frame[4]
                    )),
                left = image.centerX,
                top = image.centerY,
                right = image.centerX + image.centerWidth,
                bottom = image.centerY + image.centerHeight
            )
        )
    }

    // Load pixel fonts into bitMapFonts map
    assetConfig.pixelFonts.forEach { (fontName, fontImage) ->
        val assetFolder = this.parent.path
        val ext = fontImage.type

        // Load and set bitmap fonts
        // IDEA: check if font file is json, xml, or txt and call the appropriate loader - check BitmapFont.kt in Korge
        val bitmapFont = resourcesVfs["${assetFolder}/${fontName}.${ext}"].readFontTxt { pngName ->
            // Load bmpSlice for pixel font from texture atlas
            textureAtlases.getOrElse(index = fontImage.frame[0]) { error("readKorgeFleksAssets - texture atlas index '${fontImage.frame[0]}' for pixel font image '$fontName' not found!") }
                .slice(RectangleInt(
                    x = fontImage.frame[1],
                    y = fontImage.frame[2],
                    width = fontImage.frame[3],
                    height = fontImage.frame[4]
                ))
        }
        bitMapFonts[fontName] = Pair(type, bitmapFont)
    }

    // Load parallax layers into parallaxTextures map
    assetConfig.parallaxLayers.forEach { (name, parallaxInfo) ->

        fun setBmpSlice(layer: ParallaxLayer) {
            layer.bmpSlice = textureAtlases.getOrElse(index = layer.frame[0]) { error("readKorgeFleksAssets - texture atlas index '${layer.frame[0]}' for parallax info '$name' not found!") }
                .slice(RectangleInt(
                    x = layer.frame[1],
                    y = layer.frame[2],
                    width = layer.frame[3],
                    height = layer.frame[4]
                ))
        }

        parallaxInfo.backgroundLayers.forEach { layer -> setBmpSlice(layer) }
        parallaxInfo.foregroundLayers.forEach { layer -> setBmpSlice(layer) }

        parallaxInfo.parallaxPlane?.let { planeInfo ->

            fun setBmpSlice(line: LineTexture) {
                line.bmpSlice = textureAtlases.getOrElse(index = line.frame[0]) { error("readKorgeFleksAssets - texture atlas index '${line.frame[0]}' for parallax info '$name' not found!") }
                    .slice(RectangleInt(
                        x = line.frame[1],
                        y = line.frame[2],
                        width = line.frame[3],
                        height = line.frame[4]
                    ))
            }

            planeInfo.topAttachedLayers.forEach { layer -> setBmpSlice(layer) }
            planeInfo.bottomAttachedLayers.forEach { layer -> setBmpSlice(layer) }
            planeInfo.lineTextures.forEach { line -> setBmpSlice(line) }
        }

        parallaxLayers[name] = Pair(type, parallaxInfo)
    }

    // Load tileset atlases
    val tilesetAtlases: List<BmpSlice> = assetConfig.tilesets.map { tileset ->
        parent[tileset].readBitmapSlice(props = ImageDecodingProps.DEFAULT)
    }

    // Load tiles into tilesets map
    assetConfig.tiles.frames.forEach { (name, frames) ->
        // TODO
    }
}


class TextureAtlasLoader {
    fun loadTilemapsTilesets(
        type: AssetType,
        atlas: Atlas,
        config: TextureConfig,
        tilesets: TilesetMapType
    ) {
        // Cache tiles into tilesets
        val tilesetCache: MutableMap<String, MutableMap<Int, RectSlice<out Bitmap>>> = mutableMapOf()
        atlas.entries.forEachWith("tls_") { entry, frameTag ->
            // Get tileset name
            val tileNumberRegex = "_tile(\\d+)$".toRegex()
            val tilesetName = frameTag.replace(tileNumberRegex, "")
            // Get the tile index number
            val match = tileNumberRegex.find(frameTag)
            val tileIndex = match?.groupValues?.get(1)?.toInt() ?: error("TextureAtlasLoader - Cannot get tile index of texture '${frameTag}'!")
            val bmpSlice = atlas.texture.slice(entry.info.frame)

            // Store tile slice in tileset cache
            if (tilesetCache.containsKey(tilesetName)) tilesetCache[tilesetName]!![tileIndex] = bmpSlice
            else tilesetCache[tilesetName] = mutableMapOf(tileIndex to bmpSlice)
        }

        // Create tileset objects from cached tile slices
        config.tilesets.forEach { tilesetConfig ->
            val emptySlice: RectSlice<out Bitmap> = Bitmap32.EMPTY.slice()

            val tilesetName = tilesetConfig.name
            if (tilesetCache.containsKey(tilesetName)) {
                val tileCount = tilesetConfig.size
                val tileset = TileSet(
                    (0 until tileCount).map { index ->
//                        println("Tileset: $tilesetName Tile: $index / $tileCount")

                        val bmpSlice = if (tilesetCache[tilesetName]!!.containsKey(index)) tilesetCache[tilesetName]!![index]!!
                        else emptySlice

                        TileSetTileInfo(
                            // This is the tile id which is used in TileMapData to reference this tile
                            index, bmpSlice)

                    },
                    width = 16,
                    height = 16,
                    border = 0
                )
                tilesets[tilesetName] = Pair(type, tileset)
            } else println("WARNING: TextureAtlasLoader - Cannot find tiles for '$tilesetName' in texture atlas!")
        }
/*
        config. .forEach { (levelName, tileMapConfig) ->
            val ldtkFile = tileMapConfig.fileName
            val collisionLayerName = tileMapConfig.collisionLayerName
//            fun emptyTileSetTileInfo(index: Int) = TileSetTileInfo(index, Bitmap32.EMPTY.slice())
            val emptySlice: RectSlice<out Bitmap> = Bitmap32.EMPTY.slice()

            // First we need to load the LDtk world because we need to get the number of tiles for the tileset object
            // Then we will load each tile from the texture atlas
            val ldtkWorld = resourcesVfs["$assetFolder/$ldtkFile"].readLdtkWorld { tilesetName, tileCount ->
                // Load bmp slice from texture atlas and store as tile in tileset

                val tileset2: List<TileFrame> = (
                    0 until tileCount).map { index ->
                    println("Tileset: $tilesetName Tile: $index / $tileCount")
                    val bmpSlice = if (tilesetCache.containsKey(tilesetName)
                        && tilesetCache[tilesetName]!!.containsKey(index)
                    ) tilesetCache[tilesetName]!![index]!!
                    else emptySlice
                    TileFrame(
                        bmpSlice,
                        // TODO set targetX and Y
                    )
                }



                val tileset = TileSet(
                    (0 until tileCount).map { index ->
//                        println("Tileset: $tilesetName Tile: $index / $tileCount")

                        val bmpSlice = if (tilesetCache.containsKey(tilesetName)
                            && tilesetCache[tilesetName]!!.containsKey(index)) tilesetCache[tilesetName]!![index]!!
                        else emptySlice

                        TileSetTileInfo(
                            // This is the tile id which is used in TileMapData to reference this tile
                            index, bmpSlice)

                    },
                    width = 16,
                    height = 16,
                    border = 0
                )
                // TODO: Check if we would need to save tileset separately for hot-reloading
                //       tilesets[tilesetName] = Pair(type, tileset)
                tileset
            }

            // TODO: Check if we need this for hot-reloading tilesets - in the end tilesets will be hot-reloaded when texture atlas changes
            val tileSetPaths = mutableListOf<String>()

            when  (type) {
                AssetType.LEVEL -> {
                    assetLevelDataLoader.loadLevelData(ldtkWorld, collisionLayerName, levelName, tileSetPaths)
                }
                else -> {
                    // Load raw tile map data for tilemap object types
                    ldtkWorld.ldtk.levels.forEach { ldtkLevel ->
                        tileMaps[ldtkLevel.identifier] = Pair(type, LayerTileMaps(levelName, ldtkWorld, ldtkLevel))
                    }
                }
            }
            println()
        }
*/
    }
}


suspend fun VfsFile.readFontTxt(
    callback: ((String) -> BmpSlice)  // callback to pass in BmpSlice from texture atlas
): BitmapFont {
    val fntFile = this
    val content = fntFile.readString().trim()
    val textures = hashMapOf<Int, BmpSlice>()
    val kernings = arrayListOf<BitmapFont.Kerning>()
    val glyphs = arrayListOf<BitmapFont.Glyph>()
    var lineHeight = 16.0
    var fontSize = 16.0
    var base: Double? = null
    for (rline in content.lines()) {
        val line = rline.trim()
        val map = LinkedHashMap<String, String>()
        for (part in line.split(' ')) {
            val (key, value) = part.split('=') + listOf("", "")
            map[key] = value
        }
        when {
            line.startsWith("info") -> {
                fontSize = (map["size"]?.toDouble() ?: 16.0).absoluteValue
            }
            line.startsWith("page") -> {
                val id = map["id"]?.toInt() ?: 0
                val file = map["file"]?.unquote() ?: error("page without file")
                textures[id] = callback.invoke(file)
            }
            line.startsWith("common ") -> {
                lineHeight = map["lineHeight"]?.toDoubleOrNull() ?: 16.0
                base = map["base"]?.toDoubleOrNull()
            }
            line.startsWith("char ") -> {
                //id=54 x=158 y=88 width=28 height=42 xoffset=2 yoffset=8 xadvance=28 page=0 chnl=0
                val page = map["page"]?.toIntOrNull() ?: 0
                val texture = textures[page] ?: textures.values.first()
                val dmap = map.dyn
                val id = dmap["id"].int
                glyphs += BitmapFont.Glyph(
                    fontSize = fontSize,
                    id = id,
                    xoffset = dmap["xoffset"].int,
                    yoffset = dmap["yoffset"].int,
                    xadvance = dmap["xadvance"].int,
                    texture = texture.sliceWithSize(dmap["x"].int, dmap["y"].int, dmap["width"].int, dmap["height"].int, "glyph-${id.toChar()}")
                )
            }
            line.startsWith("kerning ") -> {
                kernings += BitmapFont.Kerning(
                    first = map["first"]?.toIntOrNull() ?: 0,
                    second = map["second"]?.toIntOrNull() ?: 0,
                    amount = map["amount"]?.toIntOrNull() ?: 0
                )
            }
        }
    }

    return BitmapFont(
        fontSize = fontSize,
        lineHeight = lineHeight,
        base = base ?: lineHeight,
        glyphs = glyphs.associateBy { it.id }.toIntMap(),
        kernings = kernings.associateByInt { _, it ->
            BitmapFont.Kerning.buildKey(it.first, it.second)
        }
    )
}
