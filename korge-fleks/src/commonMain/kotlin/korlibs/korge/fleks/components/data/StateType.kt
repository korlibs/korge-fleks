package korlibs.korge.fleks.components.data

enum class StateType {
    ILLEGAL,

    EXPLOSION,  // Used normally only by explosion objects
    FALL,
    FALL_ATTACK,
    FLY,
    FLY_HIT,
    IDLE,
    IDLE_HIT,
    JUMP,
    JUMP_ATTACK,
    ON_FLOOR_ATTACK,
    ON_FLOOR_HIT,
    RUN,
    RUN_ATTACK,
    RUN_HIT,
    SQUAT,
    SQUAT_ATTACK,
    SQUAT_HIT,
    STAND,
    STAND_ATTACK,
    STAND_HIT,
    TURN,
    TURN_HIT,
    WAKEUP,
    WAKEUP_HIT;

    companion object {

        /** Mapping strings used in Tiled map editor to a specific game object type.
         *  Use this instead of valueOf() which throws IllegalArgumentException.
         */
        fun valueOfString(value: String): StateType {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                println("ERROR: StateType - Enum for value '$value' does not exist! Exception: $e")
                ILLEGAL
            }
        }
    }
}
