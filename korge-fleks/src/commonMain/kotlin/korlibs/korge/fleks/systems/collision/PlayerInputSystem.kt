package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem


class PlayerInputSystem() : IntervalSystem(
    interval = Fixed(1 / 60f)
) {
    // directions of digital joystick
    var up = false
    var down = false
    var right = false
    var left = false
    var justUp = false
    var justDown = false
    var justRight = false
    var justLeft = false
    var joystickXDirection = 0.0f
    var joystickYDirection = 0.0f
    var attack = false
    var justReleasedAttack = false
    var attackDirection = 0.0f
    var attackIndex = 0


    var isPressedKeyA: Boolean = false
    var isJustPressedKeyA: Boolean = false
    var isPressedKeyS: Boolean = false
    var isJustPressedKeyS: Boolean = false
    var isPressedKeyD: Boolean = false
    var isJustPressedKeyD: Boolean = false
    var isPressedKeyW: Boolean = false
    var isJustPressedKeyW: Boolean = false

    override fun onTick() {
        // get input from user via keyboard
        val newRight = isPressedKeyD  // input.isKeyPressed(Input.Keys.RIGHT) || input.isKeyPressed(Input.Keys.D)
        val newLeft = isPressedKeyA   // input.isKeyPressed(Input.Keys.LEFT) || input.isKeyPressed(Input.Keys.A)
        val newUp = isPressedKeyW     // input.isKeyPressed(Input.Keys.UP) || input.isKeyPressed(Input.Keys.W)
        val newDown = isPressedKeyS   // input.isKeyPressed(Input.Keys.DOWN) || input.isKeyPressed(Input.Keys.S)

        // only set just pressed state if the user is not still pressing the opposite key
        justRight = !(left && newLeft) && isJustPressedKeyD   // (input.isKeyJustPressed(Input.Keys.RIGHT) || input.isKeyJustPressed(Input.Keys.D))
        justLeft = !(right && newRight) && isJustPressedKeyA  // (input.isKeyJustPressed(Input.Keys.LEFT) || input.isKeyJustPressed(Input.Keys.A))
        justUp = !(down && newDown) && isJustPressedKeyW      // (input.isKeyJustPressed(Input.Keys.UP) || input.isKeyJustPressed(Input.Keys.W))
        justDown = !(up && newUp) && isJustPressedKeyS        // (input.isKeyJustPressed(Input.Keys.DOWN) || input.isKeyJustPressed(Input.Keys.S))

        // Reset just pressed keys
        isJustPressedKeyD = false
        isJustPressedKeyA = false
        isJustPressedKeyW = false
        isJustPressedKeyS = false

        // newly pressed keys does not fire if the other opposite key is still pressed
        // this is checked with the first if for horizontal and vertical key state
        if (right && newRight || left && newLeft) {
            // keep right or left when the specific key is still pressed
        } else if (newRight) {
            right = true
            left = false
        } else if (newLeft) {
            right = false
            left = true
        } else {
            left = false
            right = left
        }
        if (up && newUp || down && newDown) {
            // keep right or left when the specific key is still pressed
        } else if (newUp) {
            up = true
            down = false
        } else if (newDown) {
            up = false
            down = true
        } else {
            down = false
            up = false
        }

// TODO        // Get attack button input
//        if (Gdx.input.isTouched) {
//            attack = true
//            // Get new position for attack button
//            var y = startMouseY - Gdx.input.y
//            val maxMovement = 100
//            if (y > maxMovement) {
//                y = maxMovement
//            } else if (y < -maxMovement) {
//                y = -maxMovement
//            }
//            attackDirection = 1.0f * y / 100
//        } else {
//            justReleasedAttack = attack
//            attack = false
//            // When mouse button is released then reset gun to horizontal state
//            attackDirection = 0.0f
//            startMouseY = Gdx.input.y
//        }
//
//        // Call base class function for generic joystick functionality
//        val attackDir = attackDirection
//
//        // We need to map the attack direction [-1...1] to 17 index frames
//        val maxIndexFrames = 16
//        val midIndexFrame = maxIndexFrames / 2
//        val horizontalAttach = 0.2f
//        val maxAttach = 0.95f
//        val indexStep = (1.0f - horizontalAttach) / midIndexFrame
//        attackIndex = if (attackDir < horizontalAttach && attackDir > -horizontalAttach) {
//            // Set index to mid-point
//            midIndexFrame
//        } else if (attackDir > horizontalAttach) {
//            if (attackDir > maxAttach) {
//                // Adjust index to beginning of frame index
//                0
//            } else {
//                midIndexFrame - (attackDir / indexStep).toInt() + 1
//            }
//        } else {
//            if (attackDir < -maxAttach) {
//                maxIndexFrames
//            } else {
//                maxIndexFrames - (midIndexFrame + (attackDir / indexStep).toInt() + 1)
//            }
//        }
    }
}