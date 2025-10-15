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
import korlibs.image.tiles.TileSet
import korlibs.image.tiles.TileSetTileInfo
import korlibs.io.dynamic.dyn
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.io.util.unquote
import korlibs.korge.fleks.assets.AssetLevelDataLoader
import korlibs.korge.fleks.assets.AssetModel.TextureConfig
import korlibs.korge.fleks.assets.BitMapFontMapType
import korlibs.korge.fleks.assets.NinePatchBmpSliceMapType
import korlibs.korge.fleks.assets.ParallaxMapType
import korlibs.korge.fleks.assets.ParallaxPlaneTexturesMapType
import korlibs.korge.fleks.assets.ParallaxTexturesMapType
import korlibs.korge.fleks.assets.SpriteFramesMapType
import korlibs.korge.fleks.assets.TileMapsType
import korlibs.korge.fleks.assets.data.ParallaxConfig.Mode.HORIZONTAL_PLANE
import korlibs.korge.fleks.assets.data.ParallaxConfig.Mode.VERTICAL_PLANE
import korlibs.korge.fleks.assets.data.ParallaxPlaneTextures.LineTexture
import korlibs.korge.fleks.assets.data.SpriteFrames.*
import korlibs.korge.fleks.assets.data.ldtk.readLdtkWorld
import korlibs.math.geom.slice.RectSlice
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
//    forEach { entry ->
        if (imagePrefixRegex.containsMatchIn(entry.name)) {
            val imageName = entry.name.replace(imagePrefixRegex, "")
            val frameTag = if (frameTagRegex.containsMatchIn(imageName)) {
                imageName.replace(frameTagRegex, "")
            } else imageName
            action(entry, frameTag)
        }
    }
}

class TextureAtlasLoader {
    private fun getParallaxPlaneSpeedFactor(index: Int, size: Int, speedFactor: Float) : Float {
        val midPoint: Float = size * 0.5f
        return speedFactor * (
            // The pixel in the point of view must not stand still, they need to move with the lowest possible speed (= 1 / midpoint)
            // Otherwise the midpoint is "running" away over time
            if (index < midPoint)
                1f - (index / midPoint)
            else
                (index - midPoint + 1f) / midPoint
            )
    }

    /**
     * Load all images from the texture atlas that are prefixed with "img_"
     * and store them in the textures map as single-frame or animation Sprites.
     *
     * Also sets the frameDuration for each frame according to the config.
     */
    fun loadImages(
        type: AssetType,
        atlas: Atlas,
        config: TextureConfig,
        textures: SpriteFramesMapType
    ) {
        atlas.entries.forEachWith("img_") { entry, frameTag ->
//            println("$frameTag")

            // Check if there was already a frameTag saved for this animation
            if (textures.containsKey(frameTag)) {
                val spriteData = textures[frameTag]!!.second

                // Get the animation index number
                val regex = "_(\\d+)$".toRegex()
                val match = regex.find(entry.name)
                val animIndex = match?.groupValues?.get(1)?.toInt()
                    ?: error("TextureAtlasLoader - Cannot get animation index of sprite '${entry.name}'!")

                spriteData.add(animIndex, SpriteFrame(
                    atlas.texture.slice(entry.info.frame),
                    entry.info.virtFrame?.x ?: 0,
                    entry.info.virtFrame?.y ?: 0
                ))
            } else {
                textures[frameTag] = Pair(type, SpriteFrames(
                    frames = mutableListOf(
                        SpriteFrame(
                            atlas.texture.slice(entry.info.frame),
                            entry.info.virtFrame?.x ?: 0,
                            entry.info.virtFrame?.y ?: 0
                        )
                    ),
                    width = entry.info.virtFrame?.width ?: 0,
                    height = entry.info.virtFrame?.height ?: 0
                ))
            }
        }

        // Load and set frameDuration
        config.frameDurations.forEach { (frameTag, duration) ->
            if (textures.containsKey(frameTag)) {
                val spriteData = textures[frameTag]!!.second
                for (i in 0 until spriteData.size) {
                    if (duration.custom == null) {
                        // Set all frames to the same default duration
                        spriteData[i].duration = duration.default / 1000f  // convert ms to seconds
                    } else if (i < duration.custom.size) {
                        spriteData[i].duration = duration.custom[i] / 1000f  // convert ms to seconds
                    } else {
                        println("ERROR: TextureAtlasLoader - Cannot set custom frameDuration for '$frameTag' of - not enough durations specified in config.yaml!")
                    }
                }
            } else {
                println("ERROR: TextureAtlasLoader - Cannot set frameDuration for '$frameTag' - texture not found!")
            }
        }
//        println()
    }

    /**
     * Load all nine-patch-slice images from the texture atlas that are prefixed with "npt_"
     * and store them in the ninePatchSlices map.
     */
    fun loadNinePatchSlices(
        type: AssetType,
        atlas: Atlas,
        config: TextureConfig,
        ninePatchSlices: NinePatchBmpSliceMapType
    ) {
        atlas.entries.forEachWith("npt_") { entry, frameTag ->
            // Load nine-patch slice info from config
            val nineSlice = if (config.nineSlices.containsKey(frameTag)) config.nineSlices[frameTag]
            else error("TextureAtlasLoader - Cannot find nine-patch slice info for '$frameTag' in '$type' texture atlas config!")

            // Create nine-patch object
            ninePatchSlices[frameTag] = Pair(
                type,
                NinePatchBmpSlice.createSimple(
                    bmp = atlas.texture.slice(entry.info.frame),
                    left = nineSlice!!.x,
                    top = nineSlice.y,
                    right = nineSlice.x + nineSlice.width,
                    bottom = nineSlice.y + nineSlice.height
                )
            )
        }
//        println()
    }

    /**
     * This function loads all bitmap fonts from a texture atlas whose entry names start with the prefix pxf_.
     * For each matching entry, it attempts to load the corresponding font file (with a .fnt extension) from
     * the asset folder, using the atlas entry as the font texture. The loaded bitmap fonts are then stored in
     * the provided bitMapFonts map, keyed by font name.
     */
    suspend fun loadPixelFonts(
        type: AssetType,
        atlas: Atlas,
        config: TextureConfig,
        assetFolder: String,
        bitMapFonts: BitMapFontMapType
    ) {
        atlas.entries.forEachWith("pxf_") { entry, fontName ->
            // Sanity check for pixel font in texture atlas config
            // TODO Check if we could remove fonts entry from texture atlas config, since font images are marked with "pxf_" prefix
            if (!config.fonts.contains(fontName)) println("WARNING: TextureAtlasLoader - Cannot find font name '$fontName' in '$type' texture atlas config!")

            // Load and set bitmap fonts
            // IDEA: check if font file is json, xml, or txt and call the appropriate loader - check BitmapFont.kt in Korge
            val bitmapFont = resourcesVfs["${assetFolder}/${fontName}.fnt"].readFontTxt { pngName ->
                // Sanity check
                if (pngName != fontName) println("ERROR: TextureAtlasLoader - ${fontName}.fnt file points to another png file name '${pngName}'. Please check if this is correct!")
                // Load bmpSlice for pixel font from texture atlas
                atlas.texture.slice(entry.info.frame)
            }
            bitMapFonts[fontName] = Pair(type, bitmapFont)
        }
//        println()
    }

    /**
     * Loads all parallax background and plane layer textures from the given texture atlas.
     *
     * Iterates through atlas entries with the prefix "plx_" and assigns them to background or foreground
     * parallax layers, or to parallax planes and their attached layers, based on the provided configuration.
     * Populates the corresponding maps with the loaded textures and updates the global parallax background configuration.
     *
     * @param type The asset type for the loaded textures.
     * @param atlas The texture atlas containing the entries.
     * @param config The texture configuration, including parallax settings.
     * @param parallaxBackgroundConfig The map to store global parallax background configurations.
     * @param parallaxTextures The map to store background and foreground parallax layer textures.
     * @param parallaxPlaneTextures The map to store parallax plane and attached layer textures.
     */
    fun loadParallaxLayers(
        type: AssetType,
        atlas: Atlas,
        config: TextureConfig,
        parallaxBackgroundConfig: ParallaxMapType,
        parallaxTextures: ParallaxTexturesMapType,
        parallaxPlaneTextures: ParallaxPlaneTexturesMapType,
    ) {
        val bgLayerNames = mutableListOf<String>()
        val fgLayerNames = mutableListOf<String>()

        atlas.entries.forEachWith("plx_") { entry, frameTag ->
            config.parallaxBackgrounds.forEach { (_, parallaxConfig) ->
                // Check if frameTags are used for parallax background layers and save them in separate maps
                if (parallaxConfig.backgroundLayers.containsKey(frameTag)
                    || parallaxConfig.foregroundLayers.containsKey(frameTag)) {
                    // frameTag is used for parallax background layer
                    val layer = parallaxConfig.backgroundLayers[frameTag]
                        ?: parallaxConfig.foregroundLayers[frameTag]
                        ?: error("TextureAtlasLoader - Cannot find parallax layer '$frameTag' in '$type' parallax config!")

                    layer.layerBmpSlice = atlas.texture.slice(entry.info.frame)
                    parallaxTextures[frameTag] = Pair(type, layer)
                    if (parallaxConfig.backgroundLayers.containsKey(frameTag)) bgLayerNames.add(frameTag)
                    if (parallaxConfig.foregroundLayers.containsKey(frameTag)) fgLayerNames.add(frameTag)
                    return@forEach
                }

                // Check if frameTags are used for 2.5 D parallax plane
                if (parallaxConfig.parallaxPlane != null) {
                    val planeConfig = parallaxConfig.parallaxPlane
                    val planeName = planeConfig.name
                    val planeSpeedFactor = planeConfig.speedFactor

                    // Create object for parallax plane textures if not existing yet
                    if (!parallaxPlaneTextures.containsKey(planeName)) parallaxPlaneTextures[planeName] =
                        Pair(type, ParallaxPlaneTextures())

                    // Check if frameTag is used for parallax plane
                    if (frameTag.contains(planeName)) {
                        // Get the parallax plane index number
                        val regex = "_slice(\\d+)$".toRegex()
                        val match = regex.find(frameTag)
                        val planeIndex = match?.groupValues?.get(1)?.toInt() ?: error("Cannot get plane index of texture '${frameTag}'!")

                        parallaxPlaneTextures[planeName]!!.second.lineTextures.add(LineTexture(
                            index = planeIndex,
                            bmpSlice = atlas.texture.slice(entry.info.frame),
                            speedFactor = getParallaxPlaneSpeedFactor(planeIndex, parallaxConfig.parallaxHeight, planeSpeedFactor)
                        ))
                        return@forEach
                    }

                    // Check if frameTag is used for parallax plane attached layers
                    planeConfig.topAttachedLayers.forEach { (layerName, layerConfig) ->
                        if (frameTag.contains(layerName)) {
                            val planeIndex = layerConfig.attachIndex
                            val layerTexture = atlas.texture.slice(entry.info.frame)
                            val layerSize = when (parallaxConfig.mode) {
                                HORIZONTAL_PLANE -> layerTexture.height
                                VERTICAL_PLANE -> layerTexture.width
                                else -> error("TextureAtlasLoader - Cannot use top attached parallax layers without a parallax plane!")
                            }

                            parallaxPlaneTextures[planeName]!!.second.topAttachedLayerTextures.add(LineTexture(
                                index = if (layerConfig.attachBottomRight) planeIndex - layerSize else planeIndex,
                                bmpSlice = layerTexture,
                                speedFactor = getParallaxPlaneSpeedFactor(planeIndex, parallaxConfig.parallaxHeight, planeSpeedFactor)
                            ))
                        }
                    }
                    planeConfig.bottomAttachedLayers.forEach { (layerName, layerConfig) ->
                        if (frameTag.contains(layerName)) {
                            val planeIndex = layerConfig.attachIndex
                            val layerTexture = atlas.texture.slice(entry.info.frame)
                            val layerSize = when (parallaxConfig.mode) {
                                HORIZONTAL_PLANE -> layerTexture.height
                                VERTICAL_PLANE -> layerTexture.width
                                else -> error("TextureAtlasLoader - Cannot use bottom attached parallax layers without a parallax plane!")
                            }

                            parallaxPlaneTextures[planeName]!!.second.bottomAttachedLayerTextures.add(LineTexture(
                                index = if (layerConfig.attachBottomRight) planeIndex - layerSize else planeIndex,
                                bmpSlice = layerTexture,
                                speedFactor = getParallaxPlaneSpeedFactor(planeIndex, parallaxConfig.parallaxHeight, planeSpeedFactor)
                            ))
                        }
                    }
                }
            }
        }

        // Save the global parallax background configuration AFTER all atlas textures were processed
        config.parallaxBackgrounds.forEach { (parallaxName, parallaxConfig) ->
            val parallaxConfig = ParallaxBackgroundConfig(
                parallaxConfig.mode,
                width = 0,
                height = parallaxConfig.parallaxHeight,
                bgLayerNames.toList(),
                fgLayerNames.toList()
            )
            parallaxBackgroundConfig[parallaxName] = Pair(type, parallaxConfig)
        }
//        println()
    }



    data class TileFrame(
        val bmpSlice: BmpSlice,
        val targetX: Int = 0,  // offset from the top-left corner of the original tile if cropped
        val targetY: Int = 0
    )

    suspend fun loadTilemapsTilesets(
        type: AssetType,
        atlas: Atlas,
        config: TextureConfig,
        assetFolder: String,
        tileMaps: TileMapsType,
        assetLevelDataLoader: AssetLevelDataLoader
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
            if (tilesetCache.containsKey(tilesetName)) tilesetCache[tilesetName]!![tileIndex] = bmpSlice
            else tilesetCache[tilesetName] = mutableMapOf(tileIndex to bmpSlice)
        }

        config.tileMaps.forEach { (levelName, tileMapConfig) ->
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
