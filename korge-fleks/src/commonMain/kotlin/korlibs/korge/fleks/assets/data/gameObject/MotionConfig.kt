package korlibs.korge.fleks.assets.data.gameObject

import korlibs.math.interpolation.Ratio
import korlibs.math.interpolation.toRatio


data class MotionConfig(
    // Config for horizontal and vertical movement and acceleration
    // These values are coming from the game object YAML config
    var gravity: Float = 0f,                // defined in m/s²
    var maxHorizontalVelocity: Float = 0f,  // defined in m/s
    var maxVerticalVelocity: Float = 0f,    // defined in m/s
    var maxJumpVelocity: Float = 0f,        // defined in m : Sets the overall power for jumping
    var maxFallingVelocity: Float = 0f,     // defined in m/s
    var endJumpVelocity: Float = 0f,        // defined in m/s
    var horizontalProgress: Ratio = 0f.toRatio(),     // Factor to smooth horizontal motion at start and end: [0..1]
    var initJumpVelocityFactor: Float = 0f  // Factor which is multiplied with maxJumpVelocity and means how much of the
    // maxJumpVelocity is added every cycle to the jumping object
) {
    companion object {
        val NO_MOTION = MotionConfig()
    }
}
