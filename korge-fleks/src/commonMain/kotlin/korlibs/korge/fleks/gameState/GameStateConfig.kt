package korlibs.korge.fleks.gameState

import kotlinx.serialization.*


@Serializable @SerialName("GameStateConfig")
data class GameStateConfig(
    // Name of game
    val name: String,
    val version: Int = 0,
    var firstStart: Boolean = true,
    var world: String,
    var level: String,
    var special: String,  // Introduce array of special assets
    var startScript: String
)
