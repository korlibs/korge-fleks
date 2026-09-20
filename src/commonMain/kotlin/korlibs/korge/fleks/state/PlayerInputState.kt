package korlibs.korge.fleks.state


/** The player input state is responsible for storing the current state of the player's input.
 * It is used to determine the player's movement and actions in the game.
 */
interface PlayerInputState {
    var jump: Boolean
    var justJump: Boolean
    var moveLeft: Boolean
    var moveRight: Boolean
    var justMoveLeft: Boolean
    var justMoveRight: Boolean
    var squat: Boolean

    var shoot: Boolean
    var shootDirection: Int
    var layDown: Boolean

    companion object {
        const val NEUTRAL_DIRECTION: Int = 8  // shoot horizontal
    }
}
