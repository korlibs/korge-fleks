package korlibs.korge.fleks.assets.data

import korlibs.image.atlas.readAtlas
import korlibs.image.bitmap.asNinePatchSimple
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.AssetModel.TextureConfig
import kotlin.collections.set


typealias TextureMap = MutableMap<String, Pair<AssetType, SpriteAnimFrames>>

class TextureAtlasLoader {

    suspend fun load(assetFolder: String, config: TextureConfig, textures: TextureMap, type: AssetType) {
        val spriteAtlas = resourcesVfs[assetFolder + "/" + config.fileName].readAtlas()
        spriteAtlas.entries.forEach { entry ->
            //println("sprite: ${entry.name}")

            val regex = "_\\d+$".toRegex()
            if (regex.containsMatchIn(entry.name)) {
                // entry is part of a sprite animation
                val frameTag = entry.name.replace(regex, "")
                // Get the animation index number
                val regex = "_(\\d+)$".toRegex()
                val match = regex.find(entry.name)
                val animIndex = match?.groupValues?.get(1)?.toInt() ?: error("Cannot get animation index of sprite '${entry.name}'!")
                if (textures.containsKey(frameTag)) {
                    val spriteData = textures[frameTag]!!.second
                    spriteData.add(animIndex, SpriteFrame(
                        spriteAtlas.texture.slice(entry.info.frame),
                        entry.info.virtFrame?.x ?: 0,
                        entry.info.virtFrame?.y ?: 0
                    ))
                } else {
                    val spriteSourceSize = entry.info.virtFrame
                    val spriteAnimFrame = SpriteAnimFrames(
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
            } else {
                // entry is not part of a sprite animation
                val spriteSourceSize = entry.info.virtFrame
                val spriteAnimFrame = SpriteAnimFrames(
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
                textures[entry.name] = Pair(type, spriteAnimFrame)
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
                spriteData.ninePatchSlice = spriteData.firstFrame.asNinePatchSimple(nineSlice.x, nineSlice.y , nineSlice.x + nineSlice.width, nineSlice.y + nineSlice.height)
            } else {
                println("ERROR: TextureAtlasLoader.load() - Cannot set nineSlice for '$frameTag' - texture not found!")
            }
        }
    }
}
