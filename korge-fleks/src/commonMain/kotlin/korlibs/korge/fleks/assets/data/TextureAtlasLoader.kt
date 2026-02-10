package korlibs.korge.fleks.assets.data

import korlibs.datastructure.associateByInt
import korlibs.datastructure.toIntMap
import korlibs.image.atlas.Atlas
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.NinePatchBmpSlice
import korlibs.image.font.BitmapFont
import korlibs.image.format.ImageDecodingProps
import korlibs.image.format.readBitmapSlice
import korlibs.io.dynamic.dyn
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.io.util.unquote
import korlibs.korge.fleks.assets.BitMapFontsAssetType
import korlibs.korge.fleks.assets.NinePatchBmpSlicesAssetType
import korlibs.korge.fleks.assets.ParallaxLayersAssetType
import korlibs.korge.fleks.assets.SpriteFramesAssetType
import korlibs.korge.fleks.assets.TileMapsAssetType
import korlibs.korge.fleks.assets.TileSetsAssetType
import korlibs.korge.fleks.assets.data.ClusterAssetInfo.ParallaxLayersInfo.ParallaxPlane.*
import korlibs.korge.fleks.assets.data.ClusterAssetInfo.ParallaxLayersInfo.ParallaxLayer
import korlibs.korge.fleks.assets.data.SpriteFrames.*
import korlibs.math.geom.RectangleInt
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
    clusterName: String,
    textures: SpriteFramesAssetType,
    ninePatchSlices: NinePatchBmpSlicesAssetType,
    bitMapFonts: BitMapFontsAssetType,
    parallaxLayers: ParallaxLayersAssetType,
    tileSets: TileSetsAssetType,
    tileMaps: TileMapsAssetType
) {
    val clusterName: AssetType = clusterName
    // Enable ignoreUnknownKeys for testing when needed
    //val assetConfig: AssetConfig = Json { ignoreUnknownKeys = true }.decodeFromString(this.readString())
    val clusterAssetInfo: ClusterAssetInfo = Json.decodeFromString(this.readString())

    // Get version info
    val major: Int = clusterAssetInfo.version[0]
    val minor: Int = clusterAssetInfo.version[1]
    val build: Int = clusterAssetInfo.version[2]
    // Check later if asset version/build is compatible otherwise convert to new version

    val textureAtlases: List<BmpSlice> = clusterAssetInfo.textures.map { texture ->
        parent[texture].readBitmapSlice(props = ImageDecodingProps.DEFAULT)
    }

    // Load images and store into textures map
    clusterAssetInfo.images.forEach { (name, image) ->
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
            clusterName, SpriteFrames(
                frames = frames.toMutableList(),
                width = image.width,
                height = image.height
            )
        )
    }

    // Load nine-patch images and store into ninePatchSlices map
    clusterAssetInfo.ninePatches.forEach { (name, image) ->
        ninePatchSlices[name] = Pair(
            clusterName, NinePatchBmpSlice.createSimple(
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
    clusterAssetInfo.pixelFonts.forEach { (fontName, fontImage) ->
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
        bitMapFonts[fontName] = Pair(clusterName, bitmapFont)
    }

    // Load parallax layers into parallaxTextures map
    clusterAssetInfo.parallaxLayers.forEach { (name, parallaxInfo) ->

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

        parallaxLayers[name] = Pair(clusterName, parallaxInfo)
    }

    // Load tileset atlases
    val tilesetAtlases: List<BmpSlice> = clusterAssetInfo.tilesets.map { tileset ->
        parent[tileset].readBitmapSlice(props = ImageDecodingProps.DEFAULT)
    }

    // Create tile set info and store into tileSets map
    if (clusterAssetInfo.tiles.frames.isNotEmpty()) {
        val tileset = SimpleTileSet(
            tiles = clusterAssetInfo.tiles.frames,
            tilesetAtlases = tilesetAtlases,
            tileWidth = clusterAssetInfo.tiles.tileWidth,
            tileHeight = clusterAssetInfo.tiles.tileHeight
        )
        tileSets[clusterName] = Pair(clusterName, tileset)
    }

    // Load tilemap objects and store into tilemaps map
    clusterAssetInfo.tileMaps.forEach { (name, tileMapInfo) ->
        // Sanity check
        // TODO move this check into gradle plugin
        if (tileMapInfo.stackedTileMapData.size > 4096)
            error("readKorgeFleksAssets - tile map '${name}' has more than 4096 chunks which is currently not supported!")

        val chunkLevelMap = chunkLevelMap {
            tileMapInfo.stackedTileMapData.forEachIndexed { idx, tiles ->
                tiles.forEachIndexed { stackIdx, tile ->
                    if (tile != -1) stackedTiles[idx][stackIdx] = tile
                    else {
                        // Mark end of tile stack with -1 in the tile map data
                        stackedTiles[idx][stackIdx] = -1
                        return@forEachIndexed
                    }
                }
            }

            tileMapInfo.clusterList.forEachIndexed { idx, name ->
                clusterList[idx] = name
            }

            gridWidth = tileMapInfo.gridWidth
            gridHeight = tileMapInfo.gridWidth
            gridSize = tileMapInfo.gridSize
        }
        tileMaps[name] = Pair(clusterName, chunkLevelMap)
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
