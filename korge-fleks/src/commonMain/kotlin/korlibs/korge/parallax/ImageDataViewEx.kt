package korlibs.korge.parallax

import korlibs.image.format.ImageAnimation
import korlibs.korge.view.Container
import korlibs.korge.view.View
import korlibs.korge.view.ViewDslMarker
import korlibs.korge.view.addTo
import korlibs.image.format.ImageData
import korlibs.time.TimeSpan

/**
 * With imageDataView it is possible to display an image inside a Container or View.
 * It supports layers and animations. Animations consist of a series of frames which
 * are defined e.g. by tag names in Aseprite files.
 *
 * The image can be repeating in X and/or Y direction. That needs to be enabled by setting
 * repeating to true. The repeating values can be set per layer as repeatX and repeatY.
 */
inline fun Container.imageDataViewEx(
    data: ImageData? = null,
    animation: String? = null,
    playing: Boolean = false,
    smoothing: Boolean = true,
    repeating: Boolean = false,
    block: @ViewDslMarker ImageDataViewEx.() -> Unit = {}
)
    = ImageDataViewEx(data, animation, playing, smoothing, repeating).addTo(this, block)

/**
 * @example
 *
 * val ase = resourcesVfs["vampire.ase"].readImageDataWithAtlas(ASE)
 *
 * val character = imageDataViewEx(ase, "down") { stop() }
 *
 * addUpdater {
 *     val left = keys[Key.LEFT]
 *     val right = keys[Key.RIGHT]
 *     val up = keys[Key.UP]
 *     val down = keys[Key.DOWN]
 *     if (left) character.x -= 2.0
 *     if (right) character.x += 2.0
 *     if (up) character.y -= 2.0
 *     if (down) character.y += 2.0
 *     character.animation = when {
 *         left -> "left"; right -> "right"; up -> "up"; down -> "down"
 *         else -> character.animation
 *     }
 *     if (left || right || up || down) {
 *         character.play()
 *     } else {
 *         character.stop()
 *         character.rewind()
 *     }
 * }
 */
open class ImageDataViewEx(
    data: ImageData? = null,
    animation: String? = null,
    playing: Boolean = false,
    smoothing: Boolean = true,
    repeating: Boolean = false
) : Container() {
    private val animationView: ImageAnimationView<*> = if (repeating) repeatedImageAnimationView() else imageAnimationView()

    fun getLayer(name: String): View? {
        return animationView.getLayer(name)
    }

    var currentFrameIndex: Int = 0
        private set
        get() = animationView.currentFrameIndex

    var onPlayFinished: (() -> Unit)? = null

    var smoothing: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                animationView.smoothing = value
            }
        }

    var data: ImageData? = data
        set(value) {
            if (field !== value) {
                field = value
                updatedDataAnimation()
            }
        }

    var animation: String? = animation
        set(value) {
            if (field !== value) {
                field = value
                updatedDataAnimation()
            }
        }

    val animationNames: Set<String> get() = data?.animationsByName?.keys ?: emptySet()

    init {
        updatedDataAnimation()
        if (playing) play() else stop()
        this.smoothing = smoothing
        animationView.onPlayFinished = { onPlayFinished?.invoke() }
    }

    fun play(reverse : Boolean = false, once: Boolean = false) {
        animationView.direction = if (!reverse && !once) ImageAnimation.Direction.FORWARD
        else if (!reverse && once) ImageAnimation.Direction.ONCE_FORWARD
        else if (reverse && !once) ImageAnimation.Direction.REVERSE
        else ImageAnimation.Direction.ONCE_REVERSE
        animationView.play()
    }
    fun stop() { animationView.stop() }
    fun rewind() { animationView.rewind() }
    fun update(time: TimeSpan) { animationView.update(time)}

    private fun updatedDataAnimation() {
        animationView.animation = if (animation != null) data?.animationsByName?.get(animation) else data?.defaultAnimation
    }
}
