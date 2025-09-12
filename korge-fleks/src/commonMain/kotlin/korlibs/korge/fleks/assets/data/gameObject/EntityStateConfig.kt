package korlibs.korge.fleks.assets.data.gameObject

import korlibs.image.format.ImageAnimation.Direction


data class EntityStateConfig(
    val frameTag: String = "",           // Name of the frame (animation) tag in Aseprite
    val isAnimation: Boolean = false,    // Set to true if state frames are an animation

    val direction: Direction = Direction.ONCE_FORWARD,  // Default: Get direction from Aseprite file
    val destroyOnAnimationFinished: Boolean = false,    // Delete entity when direction is [ONCE_FORWARD] or [ONCE_REVERSE] and animation is finished

    val disable: Boolean = false,        // Set to true if entity shall be invisible in this state

)
