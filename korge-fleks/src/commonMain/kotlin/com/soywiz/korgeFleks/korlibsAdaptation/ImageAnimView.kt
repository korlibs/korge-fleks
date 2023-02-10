package com.soywiz.korgeFleks.korlibsAdaptation

import com.soywiz.klock.TimeSpan
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.animation.ImageDataView
import com.soywiz.korgeFleks.utils.KorgeViewBase
import com.soywiz.korim.format.ImageData
import com.soywiz.korim.format.ImageAnimation
import com.soywiz.korma.annotations.ViewDslMarker

/**
 * With imageDataView it is possible to display an image inside a Container or View.
 * It supports layers and animations. Animations consist of a series of frames which
 * are defined e.g. by tag names in Aseprite files.
 *
 * The image can be repeating in X and/or Y direction. That needs to be enabled by setting
 * repeating to true. The repeating values can be set per layer as repeatX and repeatY.
 */
inline fun Container.imageAnimView(
    data: ImageData? = null,
    animation: String? = null,
    playing: Boolean = false,
    smoothing: Boolean = true,
    repeating: Boolean = false,
    block: @ViewDslMarker ImageAnimView.() -> Unit = {}
) = ImageAnimView(data, animation, playing, smoothing, repeating).addTo(this, block)

open class ImageAnimView(
    data: ImageData? = null,
    animation: String? = null,
    playing: Boolean = false,
    smoothing: Boolean = true,
    repeating: Boolean = false
) : Container(), KorgeViewBase {
    private val animationView = if (repeating) repeatedImageAnimationView() else imageAnimationView()

    override fun getLayer(name: String): View? = animationView.getLayer(name)

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
