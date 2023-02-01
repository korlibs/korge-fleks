package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.soywiz.korge.view.View
import com.soywiz.korim.color.RGBA
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.utils.KorgeViewCache

/**
 * This system is updating the view objects for all drawable entities.
 */
class KorgeIntroParallaxViewSystem(
    private val korgeViewCache: KorgeViewCache = inject("normalViewCache")
) : IteratingSystem(
    family { all(PositionShape, Appearance, ParallaxIntro) },
    interval = EachFrame
) {
    // NOTE: Layer names should be exclusive also across parallax backgrounds which are in use at the same time
    private val layers: MutableMap<String, View> = mutableMapOf()

    fun addLayer(name: String, layer: View) {
        layers[name] = layer
    }

    fun getLayer(name: String) : View {
        return layers[name] ?: error("KorgeIntroParallaxViewSystem: Layer '$name' not found!")
    }

    fun removeLayer(name: String) : View {
        return layers.remove(name) ?: error("KorgeIntroParallaxViewSystem: Cannot remove layer with unknown name '$name'!")
    }

    override fun onTickEntity(entity: Entity) {
        val positionShape = entity[PositionShape]
        val appearance = entity[Appearance]

        korgeViewCache[entity].let { view ->
            if (appearance.visible) {
                view.visible = true
                view.alpha = appearance.alpha
                appearance.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }
                // Intentionally do not move parallax view in x direction - it is done internally in ParallaxView
                view.y = positionShape.y
            } else {
                view.visible = false
            }
        }
    }
}
