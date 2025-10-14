package korlibs.korge.fleks.assets.data

import korlibs.datastructure.associateByInt
import korlibs.datastructure.toIntMap
import korlibs.image.atlas.readAtlas
import korlibs.image.bitmap.BmpSlice
import korlibs.image.bitmap.asNinePatchSimple
import korlibs.image.font.BitmapFont
import korlibs.io.dynamic.dyn
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.io.util.unquote
import korlibs.korge.fleks.assets.AssetModel.TextureConfig
import korlibs.korge.fleks.assets.BitMapFontMapType
import korlibs.korge.fleks.assets.NinePatchBmpSliceMapType
import korlibs.korge.fleks.assets.ParallaxMapType
import korlibs.korge.fleks.assets.ParallaxPlaneTexturesMapType
import korlibs.korge.fleks.assets.ParallaxTexturesMapType
import korlibs.korge.fleks.assets.SpriteFramesMapType
import korlibs.korge.fleks.assets.data.SpriteFrames.*
import korlibs.korge.fleks.assets.data.ParallaxConfig.Mode.*
import korlibs.korge.fleks.assets.data.ParallaxPlaneTextures.LineTexture
import kotlin.collections.set
import kotlin.math.absoluteValue


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

    suspend fun load(
        assetFolder: String,
        config: TextureConfig,
        textures: SpriteFramesMapType,
        ninePatchSlices: NinePatchBmpSliceMapType,
        bitMapFonts: BitMapFontMapType,
        parallaxBackgroundConfig: ParallaxMapType,
        parallaxTextures: ParallaxTexturesMapType,
        parallaxPlaneTextures: ParallaxPlaneTexturesMapType,
        type: AssetType
    ) {
        val spriteAtlas = resourcesVfs["${assetFolder}/${config.fileName}"].readAtlas()
        val bgLayerNames = mutableListOf<String>()
        val fgLayerNames = mutableListOf<String>()

        spriteAtlas.entries.forEach { entry ->
            //println("sprite: ${entry.name}")

            val regex = "_\\d+$".toRegex()
            val frameTag = if (regex.containsMatchIn(entry.name)) {
                // entry is part of a sprite animations
                entry.name.replace(regex, "")
            } else entry.name

            // Check if frameTags are used for parallax background layers and save them in separate maps
            var textureUsedForParallaxBackground = false
            config.parallaxBackgrounds.forEach { (_, parallaxConfig) ->
                if (parallaxConfig.backgroundLayers.containsKey(frameTag)
                    || parallaxConfig.foregroundLayers.containsKey(frameTag)) {
                    // frameTag is used for parallax background layer
                    val layer = parallaxConfig.backgroundLayers[frameTag]
                        ?: parallaxConfig.foregroundLayers[frameTag]
                        ?: error("Cannot find parallax layer '$frameTag' in parallax config!")

//                    if (frameTag == "parallax_sky") {
//                        println()
//                    }

                    layer.layerBmpSlice = spriteAtlas.texture.slice(entry.info.frame)
                    parallaxTextures[frameTag] = Pair(type, layer)
                    bgLayerNames.add(frameTag)

                    textureUsedForParallaxBackground = true
                    return@forEach
                }

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
                            bmpSlice = spriteAtlas.texture.slice(entry.info.frame),
                            speedFactor = getParallaxPlaneSpeedFactor(planeIndex, parallaxConfig.parallaxHeight, planeSpeedFactor)
                        ))
                        textureUsedForParallaxBackground = true
                        return@forEach
                    }

                    // Check if frameTag is used for parallax plane attached layers
                    planeConfig.topAttachedLayers.forEach { (layerName, layerConfig) ->
                        if (frameTag.contains(layerName)) {
                            val planeIndex = layerConfig.attachIndex
                            val layerTexture = spriteAtlas.texture.slice(entry.info.frame)
                            val layerSize = when (parallaxConfig.mode) {
                                HORIZONTAL_PLANE -> layerTexture.height
                                VERTICAL_PLANE -> layerTexture.width
                                else -> error("Cannot use attached parallax layers without a parallax plane!")
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
                            val layerTexture = spriteAtlas.texture.slice(entry.info.frame)
                            val layerSize = when (parallaxConfig.mode) {
                                HORIZONTAL_PLANE -> layerTexture.height
                                VERTICAL_PLANE -> layerTexture.width
                                else -> error("Cannot use attached parallax layers without a parallax plane!")
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

            // Check for tilesets
            config.tilesets.forEach { tilesetName ->
                if (frameTag.contains(tilesetName)) {
                    // Get the tile index number
                    val regex = "_tile(\\d+)".toRegex()
                    val match = regex.find(frameTag)
                    val tileIndex = match?.groupValues?.get(1)?.toInt() ?: error("Cannot get tile index of texture '${frameTag}'!")



                }
            }



            // Save other textures
            if (!textureUsedForParallaxBackground) {
                if (textures.containsKey(frameTag)) {
                    val spriteData = textures[frameTag]!!.second

                    // Get the animation index number
                    val regex = "_(\\d+)$".toRegex()
                    val match = regex.find(entry.name)
                    val animIndex = match?.groupValues?.get(1)?.toInt()
                        ?: error("Cannot get animation index of sprite '${entry.name}'!")

                    spriteData.add(animIndex, SpriteFrame(
                        spriteAtlas.texture.slice(entry.info.frame),
                        entry.info.virtFrame?.x ?: 0,
                        entry.info.virtFrame?.y ?: 0
                    ))
                } else {
                    textures[frameTag] = Pair(type, SpriteFrames(
                        frames = mutableListOf(
                            SpriteFrame(
                                spriteAtlas.texture.slice(entry.info.frame),
                                entry.info.virtFrame?.x ?: 0,
                                entry.info.virtFrame?.y ?: 0
                            )
                        ),
                        width = entry.info.virtFrame?.width ?: 0,
                        height = entry.info.virtFrame?.height ?: 0
                    ))
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
                        println("ERROR: TextureAtlasLoader.load() - Cannot set custom frameDuration for '$frameTag' of - not enough durations specified in config.yaml!")
                    }
                }
            } else {
                println("ERROR: TextureAtlasLoader.load() - Cannot set frameDuration for '$frameTag' - texture not found!")
            }
        }

        // Load and set ninePatchBmpSlice objects
        config.nineSlices.forEach { (frameTag, nineSlice) ->
            if (textures.containsKey(frameTag)) {
                val spriteData = textures[frameTag]!!.second
                ninePatchSlices[frameTag] = Pair(
                    type,
                    spriteData.firstFrame.asNinePatchSimple(
                        nineSlice.x,
                        nineSlice.y,
                        nineSlice.x + nineSlice.width,
                        nineSlice.y + nineSlice.height
                    )
                )
            } else {
                println("ERROR: TextureAtlasLoader.load() - Cannot create nine-patch-slice for '$frameTag' - texture not found!")
            }
        }

        // Load and set bitmap fonts
        config.fonts.forEach { font ->
            // IDEA: check if font file is json, xml, or txt and call the appropriate loader - check BitmapFont.kt in Korge
            val bitmapFont = resourcesVfs["${assetFolder}/${font}.fnt"].readFontTxt { fontName ->
                if (textures.containsKey(fontName)) {
                    textures[fontName]!!.second.firstFrame
                } else error("Cannot find font texture '$fontName' for bitmap font '$font'!")
            }
            bitMapFonts[font] = Pair(type, bitmapFont)
        }

        println()
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
