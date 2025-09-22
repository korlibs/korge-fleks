package korlibs.korge.fleks.assets.data

import korlibs.image.atlas.readAtlas
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.fleks.assets.AssetModel.TextureConfig
import kotlin.collections.set


typealias textureMap = MutableMap<String, Pair<AssetType, SpriteAnimFrames>>

class TextureAtlasLoader {

    suspend fun load(assetFolder: String, config: TextureConfig, textures: textureMap, type: AssetType) {
        val spriteAtlas = resourcesVfs[assetFolder + "/" + config.fileName].readAtlas()
        spriteAtlas.entries.forEach { entry ->
            //println("sprite: ${entry.name}")

            val regex = "_\\d+$".toRegex()
            if (regex.containsMatchIn(entry.name)) {
                // entry is part of a sprite animation
                val spriteName = entry.name.replace(regex, "")
                // Get the animation index number
                val regex = "_(\\d+)$".toRegex()
                val match = regex.find(entry.name)
                val animIndex = match?.groupValues?.get(1)?.toInt() ?: error("Cannot get animation index of sprite '${entry.name}'!")
                if (textures.containsKey(spriteName)) {
                    val spriteData = textures[spriteName]!!.second
                    spriteData.add(animIndex, SpriteFrame(
                        entry.texture,
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
                            entry.texture,
                            spriteSourceSize?.x ?: 0,
                            spriteSourceSize?.y ?: 0
                        )
                    )
                    textures[spriteName] = Pair(type, spriteAnimFrame)
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
                        entry.texture,
                        spriteSourceSize?.x ?: 0,
                        spriteSourceSize?.y ?: 0
                    )
                )
                textures[entry.name] = Pair(type, spriteAnimFrame)
            }
        }
        //println()

        // TODO set frameDuration
    }
}