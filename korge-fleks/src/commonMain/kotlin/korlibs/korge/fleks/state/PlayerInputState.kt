package korlibs.korge.fleks.state


/** * The player input state is responsible for storing the current state of the player's input.
 * It is used to determine the player's movement and actions in the game.
 */
interface PlayerInputState {
    // Left and right joystick analog values, which can be used for more precise movement and aiming. These values are
    // typically in the range of [-1.0, 1.0], where (0, 0) represents the neutral position of the joystick.
    var lx: Float
    var ly: Float
    var rx: Float
    var ry : Float

    // Left joystick digital directions, which are triggered when the left knob is moved beyond a certain threshold in each direction
    // can be used to allow the player to use the virtual joystick like a D-pad if they prefer that over analog input
    var ldx: DigitalHorDir  // [-1, 0, 1]
    var ldy: DigitalVerDir  // [-1, 0, 1]

    // TODO Cleanup this
    var attack: Boolean
    var justReleasedAttack: Boolean
    var attackDirection: Float
    var attackIndex: Int
}

/**
 * DigitalDirection represents the digital direction of a joystick, which can be left, right, up, down, or neutral.
 * It is used to determine the player's movement and actions in the game when using a virtual joystick.
 */
enum class DigitalHorDir(val value: Int) {
    LEFT(-1),
    H_NEUTRAL(0),
    RIGHT(1);

    companion object {
        fun fromValue(value: Int): DigitalHorDir {
            return DigitalHorDir.entries.firstOrNull { it.value == value } ?: H_NEUTRAL
        }
    }
}

enum class DigitalVerDir(val value: Int) {
    DOWN(-1),
    V_NEUTRAL(0),
    UP(1);

    companion object {
        fun fromValue(value: Int): DigitalVerDir {
            return DigitalVerDir.entries.firstOrNull { it.value == value } ?: V_NEUTRAL
        }
    }
}
