package korlibs.korge.fleks.state

import kotlinx.serialization.*


@Serializable @SerialName("GameStateConfig")
data class GameStateConfig(
    // Name of game
    val name: String,
    val version: Int = 0,
    var firstStart: Boolean = true,
    var worldName: String,
    var initialChunkList: List<Int>,
    var startScript: String
)
