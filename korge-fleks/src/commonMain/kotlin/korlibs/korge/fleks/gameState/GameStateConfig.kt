package korlibs.korge.fleks.gameState

import kotlinx.serialization.*


@Serializable @SerialName("GameStateConfig")
data class GameStateConfig(
    // Name of game
    val name: String,
    var firstStart: Boolean,
    var world: String,
    var level: String,
    var special: String
)
