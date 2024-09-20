package korlibs.korge.fleks.gameState

import com.github.quillraven.fleks.World
import korlibs.io.file.std.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.korge.fleks.utils.*
import korlibs.korge.ldtk.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

/**
 * The game state manager is responsible for loading and managing the game state.
 * It loads the game config file 'game_config.yaml' and the common config file 'common/config.yaml' at the start of the game.
 */
object GameStateManager {
    var assetStore: AssetStore = AssetStore()
    var firstGameStart: Boolean = true

    private lateinit var gameStateConfig: GameStateConfig
    private val gameStateSerializer = EntityConfigSerializer()

    /**
     * Register a new serializers module for the entity config serializer.
     */
    fun register(name: String, module: SerializersModule) {
        gameStateSerializer.register(name, module)
    }

    /**
     * Init function for loading common game config and assets. Call this before any KorGE Scene is active.
     */
    suspend fun initGameState() {
        // Check for game config file
        val gameStateConfigVfs = resourcesVfs["game_config.yaml"]

//        if (gameStateConfigVfs.exists()) {
            // TODO Check why exits() function does not work on Android

            val gameStateConfigString = gameStateConfigVfs.readString()
            try {
                gameStateConfig = gameStateSerializer.yaml().decodeFromString<GameStateConfig>(gameStateConfigString)
            } catch (e: Throwable) {
                gameStateConfig = GameStateConfig("", true, "", "", "")
                println("ERROR: Loading game state config - $e")
            }
//        } else {
//            gameStateConfig = GameStateConfig("", true, "", "", "")
//            println("ERROR: Cannot find game state config file 'game_config.yaml'!")
//        }
        firstGameStart = gameStateConfig.firstStart

        val vfs = resourcesVfs["common/config.yaml"]
//        if (vfs.exists()) {
            // TODO Check why exits() function does not work on Android

            val gameConfigString = vfs.readString()
            try {
                val commonConfig = gameStateSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
                // Enable / disable hot reloading of common assets here
                assetStore.loadAssets(AssetType.COMMON, assetConfig = commonConfig)
            } catch (e: Throwable) { println("ERROR: Loading common assets - $e") }
//        } else println("WARNING: Cannot find common entity config file 'common/config.yaml'!")
    }

    /**
     * Load all entity configs from LDtk level and put them into the EntityFactory.
     */
    private fun loadEntityConfigsFromLdtkLevel(worldName: String, levelName: String) {
        var gameObjectCnt = 0

        val gridSize = assetStore.getLdtkWorld(worldName).ldtk.defaultGridSize
        val entityLayer = assetStore.getLdtkLevel(assetStore.getLdtkWorld(worldName), levelName).layerInstances?.firstOrNull { it.entityInstances.isNotEmpty() }
        entityLayer?.entityInstances?.forEach { entity ->
            println("INFO: Game object '${entity.identifier}' loaded for '$levelName'")

            // Create YAML string of an entity config from LDtk
            val yamlString = StringBuilder()
            // Sanity check - entity needs to have a field 'entityConfig'
            if (entity.fieldInstances.firstOrNull { it.identifier == "entityConfig" } != null) {

                yamlString.append("-   name: ${worldName}_${levelName}_${entity.identifier}_${gameObjectCnt++}\n")

                // Add position of entity
                entity.tags.firstOrNull { it == "positionable" }?.let {
                    yamlString.append("    x: ${entity.gridPos.x * gridSize}\n")
                    yamlString.append("    y: ${entity.gridPos.y * gridSize}\n")
                }

                // Add all other fields of entity
                entity.fieldInstances.forEach { field ->
                    if (field.identifier != "EntityConfig") yamlString.append("    ${field.identifier}: ${field.value}\n")
                }
                println(yamlString)

            } else println("ERROR: Game object with name '${entity.identifier}' has no field entityConfig")

            try {
                val entityConfigs: List<EntityConfig> =
                    gameStateSerializer.yaml().decodeFromString(yamlString.toString())
                entityConfigs.forEach { entityConfig ->
                    EntityFactory.register(entityConfig)
                }
                println("INFO: Entity config '${entity.identifier}' loaded for '$levelName'")
            } catch (e: Throwable) {
                println("ERROR: Loading entity config - $e")
            }
        }
    }

    /**
     * Function for loading word, level and special game config and assets. This function should be called
     * whenever new assets need to be loaded. I.e. also within a level when assets of type 'special' should
     * be loaded/reloaded.
     */
    suspend fun loadAssets() {
        // Sanity check - world needs to be always present
        if (gameStateConfig.world.isEmpty() || gameStateConfig.level.isEmpty()) {
            println("ERROR: World or level string is empty in game config! Cannot load game data!")
            return
        }

        // Start loading world assets
        val worldVfs = resourcesVfs[gameStateConfig.world + "/config.yaml"]
//        if (worldVfs.exists()) {
            // TODO Check why exits() function does not work on Android

            var gameConfigString = worldVfs.readString()
            try {
                val worldConfig = gameStateSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
                // Enable / disable hot reloading of common assets here
                assetStore.loadAssets(AssetType.WORLD, assetConfig = worldConfig)
            } catch (e: Throwable) {
                println("ERROR: Loading world assets - $e")
            }
//        } else println("WARNING: Cannot find world game config file '${gameStateConfig.world}/config.yaml'!")

        // Start loading level assets
        val levelVfs = resourcesVfs[gameStateConfig.world + "/" + gameStateConfig.level + "/config.yaml"]
//        if (levelVfs.exists()) {
            gameConfigString = levelVfs.readString()
            try {
                val worldConfig = gameStateSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
                // Enable / disable hot reloading of common assets here
                assetStore.loadAssets(AssetType.LEVEL, assetConfig = worldConfig)
            } catch (e: Throwable) {
                println("ERROR: Loading level assets - $e")
            }
//        } else println("WARNING: Cannot find level game config file '${gameStateConfig.world}/${gameStateConfig.level}/config.yaml'!")

        // Start loading special assets
        if (gameStateConfig.level.isNotEmpty()) {
            val vfs = resourcesVfs[gameStateConfig.world + "/" + gameStateConfig.level + "/" + gameStateConfig.special + "/config.yaml"]
//            if (vfs.exists()) {
                gameConfigString = vfs.readString()
                try {
                    val specialConfig = gameStateSerializer.yaml().decodeFromString<AssetModel>(gameConfigString)
                    // Enable / disable hot reloading of common assets here
                    assetStore.loadAssets(AssetType.SPECIAL, assetConfig = specialConfig)
                } catch (e: Throwable) {
                    println("ERROR: Loading special assets - $e")
                }
//            } else println("WARNING: Cannot find special game config file '${gameStateConfig.special}/${gameStateConfig.special}/config.yaml'!")
        }

        loadEntityConfigsFromLdtkLevel(gameStateConfig.world, gameStateConfig.special)  // here entity configs for "intro" are loaded
        loadEntityConfigsFromLdtkLevel(gameStateConfig.world, gameStateConfig.level)
    }

    /**
     * This function is called to start the game. It will load the start script from the current LDtk level and
     * create and configure the very first entity of the game.
     */
    fun startGame(world: World) {

        val startScript = if (gameStateConfig.firstStart) "world_1_intro_start_script_0"
            else "{${gameStateConfig.world}_${gameStateConfig.level}_start_script_0"

        if (EntityFactory.contains(startScript)) {
            println("INFO: Starting '${gameStateConfig.level}' with start script '$startScript'.")
            world.createAndConfigureEntity(startScript)
        }
        else println("Error: Cannot start '${gameStateConfig.level}'! EntityConfig with name '$startScript' does not exist!")
    }
}
