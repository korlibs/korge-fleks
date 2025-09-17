package korlibs.korge.fleks.assets.data

import korlibs.korge.fleks.assets.data.gameObject.CollisionData
import korlibs.korge.fleks.assets.data.gameObject.MotionConfig
import korlibs.korge.fleks.assets.data.gameObject.StateConfig
import korlibs.korge.fleks.components.data.StateType

class GameObjectConfig (
    val states: Map<StateType, StateConfig>,
    val motion: MotionConfig? = null,  // Optional motion config, if not defined then the game object is static
) {
    fun getCollisionData(state: StateType) : CollisionData {
        val stateConfig = states[state]
        if (stateConfig != null) {
            if (stateConfig.collisionBox != null) {
                return stateConfig.collisionBox
            } else {
                println("ERROR: GameObjectConfig - No collision box defined for state $state!")
            }
            return stateConfig.collisionBox ?: CollisionData(0, 0, 0f, 0f)
        } else {
            println("ERROR: GameObjectConfig - No state config defined for state $state!")
            return CollisionData(0, 0, 0f, 0f)
        }

    }

    fun getMotionConfig() : MotionConfig {
        if (motion != null) return motion
        else {
            println("ERROR: GameObjectConfig - No motion config defined for game object!")
            return MotionConfig.NO_MOTION
        }

    }
}