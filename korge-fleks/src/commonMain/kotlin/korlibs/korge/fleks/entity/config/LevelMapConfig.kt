package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Layer.Companion.layerComponent
import korlibs.korge.fleks.components.LevelMap.Companion.levelMapComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.systems.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This class contains the config for a specific level map with given [name].
 * The function implementation [configureEntity] creates a new [LevelMapConfig] entity and configures it
 * with the specified config details.
 *
 * This class creates a level map background entity which is used for various backgrounds in the game and intro.
 */
@Serializable @SerialName("LevelMapConfig")
data class LevelMapConfig(
    override val name: String,

    private val levelName: String,         // Unique name for level within a world
    private val layerNames: List<String>,  // List of names for layers to show
    private val layerTag: RenderLayerTag,
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val alpha: Float = 1f,
    private val enableParallax: Boolean = true
) : EntityConfig {

    override fun World.entityConfigure(entity: Entity) : Entity {
        val worldData: WorldData = inject<AssetStore>("AssetStore").getWorldData(levelName)

        // Set level size in CameraSystem for parallax effect
        if (enableParallax) {
            system<CameraSystem>().worldWidth = worldData.width
            system<CameraSystem>().worldHeight = worldData.height
        }

        entity.configure {
            it += levelMapComponent {
                levelName = this@LevelMapConfig.levelName
                layerNames.init(from = this@LevelMapConfig.layerNames)
                levelChunks = ChunkArray2(width = worldData.gridVaniaWidth, height = worldData.gridVaniaHeight)
             }
            // Level map does not have position - camera position will determine what is shown from the level map
            // Size of level map is static and can be gathered from AssetStore -> AssetLevelData
            it += rgbaComponent {
                alpha = this@LevelMapConfig.alpha
            }
            it += layerComponent {}
            it += layerTag
        }
        return entity
    }

    init {
        // Register entity config into entity factory for lookup by its name
        EntityFactory.register(this)
    }
}
