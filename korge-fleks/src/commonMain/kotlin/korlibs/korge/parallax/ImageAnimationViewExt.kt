package korlibs.korge.parallax

import korlibs.image.bitmap.Bitmaps
import korlibs.image.format.ImageAnimation
import korlibs.korge.view.Container
import korlibs.korge.view.ViewDslMarker
import korlibs.korge.view.addTo

inline fun Container.repeatedImageAnimationView(
    animation: ImageAnimation? = null,
    direction: ImageAnimation.Direction? = null,
    block: @ViewDslMarker ImageAnimationView<SingleTile>.() -> Unit = {}
) = ImageAnimationView(animation, direction) { SingleTile(Bitmaps.transparent) }.addTo(this, block)
