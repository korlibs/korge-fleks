package korlibs.korge.fleks.utils

import korlibs.math.geom.Point


class Geometry {
    var shootDirections: Array<Point> = arrayOf()
    fun isLeft(direction: Int): Boolean {
        return direction > DIRECTION_DOWN
    }

    fun isDown(direction: Int): Boolean {
        return direction < DIRECTION_LEFT && direction > DIRECTION_RIGHT
    }

    companion object {
        const val RIGHT_DIRECTION = 1
        const val LEFT_DIRECTION = -1
        const val TOP_DIRECTION = -1
        const val BOTTOM_DIRECTION = 1

        // Ray directions used during raycasting
        const val RAYCAST_UP = 0
        const val RAYCAST_DOWN = 1
        const val RAYCAST_RIGHT = 2
        const val RAYCAST_LEFT = 3

        const val DIRECTION_NONE = -1
        const val DIRECTION_0 = 0
        const val DIRECTION_1 = 1
        const val DIRECTION_2 = 2
        const val DIRECTION_3 = 3
        const val DIRECTION_4 = 4
        const val DIRECTION_5 = 5
        const val DIRECTION_6 = 6
        const val DIRECTION_7 = 7
        const val DIRECTION_8 = 8
        const val DIRECTION_9 = 9
        const val DIRECTION_10 = 10
        const val DIRECTION_11 = 11
        const val DIRECTION_12 = 12
        const val DIRECTION_13 = 13
        const val DIRECTION_14 = 14
        const val DIRECTION_15 = 15
        const val DIRECTION_16 = 16
        const val DIRECTION_17 = 17
        const val DIRECTION_18 = 18
        const val DIRECTION_19 = 19
        const val DIRECTION_20 = 20
        const val DIRECTION_21 = 21
        const val DIRECTION_22 = 22
        const val DIRECTION_23 = 23
        const val DIRECTION_24 = 24
        const val DIRECTION_25 = 25
        const val DIRECTION_26 = 26
        const val DIRECTION_27 = 27
        const val DIRECTION_28 = 28
        const val DIRECTION_29 = 29
        const val DIRECTION_30 = 30
        const val DIRECTION_31 = 31
        const val NUMBER_OF_DIRECTIONS = 32
        const val DIRECTION_DOWN = NUMBER_OF_DIRECTIONS / 2
        const val DIRECTION_RIGHT = NUMBER_OF_DIRECTIONS / 4
        const val DIRECTION_LEFT = NUMBER_OF_DIRECTIONS * 3 / 4
        const val DIRECTION_DIAGONAL_RIGHT_DOWN = NUMBER_OF_DIRECTIONS * 3 / 8
    }

    init {
// TODO
//        shootDirections.add(Vector2(0f, 1f))           // 0 : 90°
//        shootDirections.add(Vector2(0.194f, 0.981f))   // 1 : 78,8°
//        shootDirections.add(Vector2(0.383f, 0.924f))   // 2 : 67,5°
//        shootDirections.add(Vector2(0.555f, 0.832f))   // 3 : 56,3°
//        shootDirections.add(Vector2(0.707f, 0.707f))   // 4 : 45°
//        shootDirections.add(Vector2(0.832f, 0.555f))   // 5 : 33,7°
//        shootDirections.add(Vector2(0.924f, 0.383f))   // 6 : 22,5°
//        shootDirections.add(Vector2(0.981f, 0.194f))   // 7 : 11,2°
//        shootDirections.add(Vector2(1f, 0f))           // 8 : 0°
//        shootDirections.add(Vector2(0.981f, -0.194f))  // 9
//        shootDirections.add(Vector2(0.924f, -0.383f))  // 10
//        shootDirections.add(Vector2(0.832f, -0.555f))  // 11
//        shootDirections.add(Vector2(0.707f, -0.707f))  // 12
//        shootDirections.add(Vector2(0.555f, -0.832f))  // 13
//        shootDirections.add(Vector2(0.383f, -0.924f))  // 14
//        shootDirections.add(Vector2(0.194f, -0.981f))  // 15
//        shootDirections.add(Vector2(0f, -1f))          // 16 : 90°
//        shootDirections.add(Vector2(-0.194f, -0.981f)) // 17 : 78,8°
//        shootDirections.add(Vector2(-0.383f, -0.924f)) // 18 : 67,5°
//        shootDirections.add(Vector2(-0.555f, -0.832f)) // 19 : 56,3°
//        shootDirections.add(Vector2(-0.707f, -0.707f)) // 20 : 45°
//        shootDirections.add(Vector2(-0.832f, -0.555f)) // 21 : 33,7°
//        shootDirections.add(Vector2(-0.924f, -0.383f)) // 22 : 22,5°
//        shootDirections.add(Vector2(-0.981f, -0.194f)) // 23 : 11,2°
//        shootDirections.add(Vector2(-1f, 0f))          // 24 : 0°
//        shootDirections.add(Vector2(-0.981f, 0.194f))  // 25
//        shootDirections.add(Vector2(-0.924f, 0.383f))  // 26
//        shootDirections.add(Vector2(-0.832f, 0.555f))  // 27
//        shootDirections.add(Vector2(-0.707f, 0.707f))  // 28
//        shootDirections.add(Vector2(-0.555f, 0.832f))  // 29
//        shootDirections.add(Vector2(-0.383f, 0.924f))  // 30
//        shootDirections.add(Vector2(-0.194f, 0.981f))  // 31
    }
}
