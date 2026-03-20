package korlibs.korge.fleks.state


/** * The player input state is responsible for storing the current state of the player's input.
 * It is used to determine the player's movement and actions in the game.
 */
interface PlayerInputState {
    var up: Boolean
    var down: Boolean
    var right: Boolean
    var left: Boolean
    var justUp: Boolean
    var justDown: Boolean
    var justRight: Boolean
    var justLeft: Boolean
    var justReleasedUp: Boolean
    var justReleasedDown: Boolean
    var justReleasedRight: Boolean
    var justReleasedLeft: Boolean
    var lx: Float
    var ly: Float
    var rx: Float
    var ry : Float
    var attack: Boolean
    var justReleasedAttack: Boolean
    var attackDirection: Float
    var attackIndex: Int
}
