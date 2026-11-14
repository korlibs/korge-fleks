package korlibs.korge.fleks.utils

/**
 * This object holds compile time configuration for the game app.
 */
object AppConfig {

    // World to pixel ratio (# of pixels representing 1 meter)
    // = Pixel per unit
    var WORLD_TO_PIXEL_RATIO_d: Double = 16.0
    var WORLD_TO_PIXEL_RATIO_INV_d: Double = 1.0 / WORLD_TO_PIXEL_RATIO_d
        private set
    var TITLE = "jobe's Legacy"

    // Target device physical display pixels, e.g.: 3120 x 1440 -> 624 x 288  (2.166 ratio)
    // This is to be used only to create a window for JVM on desktop to have the same size ration as a mobile device
    var TARGET_VIRTUAL_WIDTH_i = 384  // target_width = target_height * ratio
        set(value) {
            field = value
            VIEW_PORT_WIDTH_i = value
            VIEW_PORT_WIDTH_HALF_d = (value / 2).toDouble()
        }
    var TARGET_VIRTUAL_HEIGHT_i = 216
        set(value) {
            field = value
            VIEW_PORT_HEIGHT_i = value
            VIEW_PORT_HEIGHT_HALF_d = (value / 2).toDouble()
        }
    var MIN_VIRTUAL_WIDTH_i = 384
        private set
    var MIN_VIRTUAL_HEIGHT_i = 216
        private set

    var VIEW_PORT_WIDTH_i = TARGET_VIRTUAL_WIDTH_i
        private set
    var VIEW_PORT_HEIGHT_i = TARGET_VIRTUAL_HEIGHT_i
        private set
    var VIEW_PORT_WIDTH_HALF_d: Double = (VIEW_PORT_WIDTH_i / 2).toDouble()
        private set
    var VIEW_PORT_HEIGHT_HALF_d: Double = (VIEW_PORT_HEIGHT_i / 2).toDouble()
        private set

    var deltaPerFrame: Double = 1.0

    // Config used by collision system
    var GRID_CELL_SIZE_i = 16
    var GRID_CELL_SIZE_d = GRID_CELL_SIZE_i.toDouble()

    /**
     * Any movement greater than this value will increase the number of steps checked between movement.
     * The more steps will break down the movement into smaller pieces to avoid skipping grid collisions.
     */
    var maxGridMovementPercent: Float = 0.33f

    // Used for component and data object pool
    const val POOL_PREALLOCATE = 1024
}
