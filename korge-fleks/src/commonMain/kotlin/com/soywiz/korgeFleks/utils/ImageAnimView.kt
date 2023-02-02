package com.soywiz.korgeFleks.utils

import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korim.format.ImageData
import com.soywiz.korim.format.ImageAnimation

open class ImageAnimView(
    data: ImageData? = null,
    animation: String? = null,
    playing: Boolean = false,
    smoothing: Boolean = true,
    repeating: Boolean = false
) : Container() {
    private val animationView = if (repeating) repeatedImageAnimationView() else imageAnimationView()

    fun getLayer(name: String): View? = animationView.getLayer(name)

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

    private fun updatedDataAnimation() {
        animationView.animation = if (animation != null) data?.animationsByName?.get(animation) else data?.defaultAnimation
    }
}
