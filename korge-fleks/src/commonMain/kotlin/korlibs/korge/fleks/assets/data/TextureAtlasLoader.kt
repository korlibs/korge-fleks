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
import korlibs.korge.fleks.assets.ParallaxTexturesMapType
import korlibs.korge.fleks.assets.SpriteFramesMapType
import kotlin.collections.set
import kotlin.math.absoluteValue


class TextureAtlasLoader {

    suspend fun load(
        assetFolder: String,
        config: TextureConfig,
        textures: SpriteFramesMapType,
        ninePatchSlices: NinePatchBmpSliceMapType,
        bitMapFonts: BitMapFontMapType,
        parallaxBackgroundConfig: ParallaxMapType,
        parallaxTextures: ParallaxTexturesMapType,
        type: AssetType
    ) {
        val spriteAtlas = resourcesVfs["${assetFolder}/${config.fileName}"].readAtlas()
        val bgLayerNames = mutableListOf<String>()
        val fgLayerNames = mutableListOf<String>()
        var size = 0

        spriteAtlas.entries.forEach { entry ->
            //println("sprite: ${entry.name}")

            val regex = "_\\d+$".toRegex()
            val frameTag = if (regex.containsMatchIn(entry.name)) {
                // entry is part of a sprite animations
                entry.name.replace(regex, "")
            } else entry.name

            var textureUsedForParallaxBackground = false
            config.parallaxBackgrounds.forEach { (parallaxName, parallaxConfig) ->
                if (parallaxConfig.backgroundLayers.containsKey(frameTag)
                    || parallaxConfig.foregroundLayers.containsKey(frameTag)) {
                    // frameTag is used for parallax background layer
                    val layer = parallaxConfig.backgroundLayers[frameTag]
                        ?: parallaxConfig.foregroundLayers[frameTag]
                        ?: error("Cannot find parallax layer '$frameTag' in parallax config!")

                    if (frameTag == "parallax_sky") {
                        println()
                    }

                    layer.layerFrame = ParallaxConfigNew.ParallaxLayerConfigNew.LayerFrame(
                        bmpSlice = spriteAtlas.texture.sliceWithSize(
                            entry.info.frame.x,
                            entry.info.frame.y,
                            entry.info.frame.width - 1,
                            entry.info.frame.height
                        ),
                        entry.info.virtFrame?.x ?: 0,
                        entry.info.virtFrame?.y ?: 0
                    )
                    parallaxTextures[frameTag] = Pair(type, layer)
                    bgLayerNames.add(frameTag)
                    size = entry.info.virtFrame?.height ?: 0

                    textureUsedForParallaxBackground = true
                    return@forEach
                }
            }

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
                    val spriteSourceSize = entry.info.virtFrame
                    val spriteAnimFrame = SpriteFrames(
                        width = entry.info.virtFrame?.width ?: 0,
                        height = entry.info.virtFrame?.height ?: 0
                    )
                    spriteAnimFrame.add(
                        SpriteFrame(
                            spriteAtlas.texture.slice(entry.info.frame),
                            spriteSourceSize?.x ?: 0,
                            spriteSourceSize?.y ?: 0
                        )
                    )
                    textures[frameTag] = Pair(type, spriteAnimFrame)
                }
            }
        }
        if (config.parallaxBackgrounds.isNotEmpty()) {
            val parallaxConfig = ParallaxBackgroundConfig(
                // TODO cleanup
                config.parallaxBackgrounds.entries.first().value.offset,
                config.parallaxBackgrounds.entries.first().value.mode,
                size.toFloat(),
                bgLayerNames.toList(),
                fgLayerNames.toList()
            )
            // TODO refactor
            parallaxBackgroundConfig["intro_background"] = Pair(type, parallaxConfig)
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
/*
        // Load and set parallax background environments
        config.parallaxBackgrounds.forEach { (parallaxName, parallaxConfig) ->
            val bgLayerNames = mutableListOf<String>()
            val fgLayerNames = mutableListOf<String>()
            var size: Int = 0
            parallaxConfig.backgroundLayers.forEach { (layerName, layer) ->
                val frames = if (textures.containsKey(layerName)) {
                    textures[layerName]!!.second
                } else {
                    println("ERROR: TextureAtlasLoader.load() - Cannot create parallax layer '$layerName' for '$parallaxName' - texture not found!")
                    SpriteFrames()
                }
                layer.layerFrame = frames.firstFrame.sliceWithSize()
                parallaxTextures[layerName] = Pair(type, layer)
                bgLayerNames.add(layerName)
                size = frames.height
            }
            parallaxConfig.foregroundLayers.forEach { (layerName, layer) ->
                val frames = if (textures.containsKey(layerName)) {
                    textures[layerName]!!.second
                } else {
                    println("ERROR: TextureAtlasLoader.load() - Cannot create parallax layer '$layerName' for '$parallaxName' - texture not found!")
                    SpriteFrames()
                }
                layer.frames = frames
                parallaxTextures[layerName] = Pair(type, layer)
                fgLayerNames.add(layerName)
                size = frames.height
            }

            // TODO load parallax ground plane and attached layers

            val parallaxConfig = ParallaxBackgroundConfig(
                parallaxConfig.offset,
                parallaxConfig.mode,
                size.toFloat(),
                bgLayerNames.toList(),
                fgLayerNames.toList()
            )
            parallaxBackgroundConfig[parallaxName] = Pair(type, parallaxConfig)
        }
*/
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
