package korlibs.korge.fleks.prefab

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.WorldChunk.Companion.WorldChunkComponent
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig
import korlibs.korge.fleks.components.messagePassing.MessagePassingConfig.Companion.MessagePassingConfigComponent
import korlibs.korge.fleks.prefab.data.LevelData
import kotlin.native.concurrent.ThreadLocal


/**
 * This class holds runtime configuration data which can be used by various systems during the game loop.
 * We need to store those data in components of entities because of serialization/deserialization of the world state.
 * Also game state rewind/fast-forward needs those data to be part of the saved world state.
 *
 * Hint: This object acts as a Prefab container for entities which hold those runtime configs.
 */
class SystemRuntimeConfigs {
    var camera: Entity? = null
    var messagePassing: Entity? = null

    fun getCameraPosition(world: World): Position? =
        if (camera != null) world.run { camera!![PositionComponent] } else null

    fun getCameraChunk(world: World): Int =
        if (camera != null) world.run { camera!![WorldChunkComponent].chunk } else 0

    fun getMessagePassingConfig(world: World): MessagePassingConfig? =
        if (messagePassing != null) world.run { messagePassing!![MessagePassingConfigComponent] } else null
}
