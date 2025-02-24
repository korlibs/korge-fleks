package korlibs.korge.fleks.utils

/**
 * This object holds compile time configuration for the game app.
 */
object AppConfig {
    // Multiplier for world size to scale the viewport up to full-HD
    var SCALE: Int = 5

    // World to pixel ratio (# of pixels representing 1 meter)
    // = Pixel per unit
    var WORLD_TO_PIXEL_RATIO = 16f
    var WORLD_TO_PIXEL_RATIO_INV = 1f / WORLD_TO_PIXEL_RATIO
    var TITLE = "jobe's Legacy"

    // Target device physical display pixels, e.g.: 3120 x 1440 -> 624 x 288  (2.166 ratio)
    // This is to be used only to create a window for JVM on desktop to have the same size ration as a mobile device
    var TARGET_VIRTUAL_WIDTH = 384  // target_width = target_height * ratio
        set(value) {
            field = value
            VIEW_PORT_WIDTH = value
            VIEW_PORT_WIDTH_HALF = (value / 2).toFloat()
        }
    var TARGET_VIRTUAL_HEIGHT = 216
        set(value) {
            field = value
            VIEW_PORT_HEIGHT = value
            VIEW_PORT_HEIGHT_HALF = (value / 2).toFloat()
        }
    var MIN_VIRTUAL_WIDTH = 384
        private set
    var MIN_VIRTUAL_HEIGHT = 216
        private set

    var VIEW_PORT_WIDTH = TARGET_VIRTUAL_WIDTH
        private set
    var VIEW_PORT_HEIGHT = TARGET_VIRTUAL_HEIGHT
        private set
    var VIEW_PORT_WIDTH_HALF: Float = (VIEW_PORT_WIDTH / 2).toFloat()
        private set
    var VIEW_PORT_HEIGHT_HALF: Float = (VIEW_PORT_HEIGHT / 2).toFloat()
        private set

    // Debug switches to enable drawing of invisible collision objects and moving raycasts
    var drawLevelMapColliders = false
    var drawDebugObjects = false
    var watchForAssetChanges = true

    var deltaPerFrame = 1.0
}
