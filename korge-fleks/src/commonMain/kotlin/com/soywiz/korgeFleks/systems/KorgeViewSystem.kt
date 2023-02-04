package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import com.soywiz.korge.annotations.KorgeExperimental
import com.soywiz.korge.bitmapfont.drawText
import com.soywiz.korge.render.useLineBatcher
import com.soywiz.korge.view.*
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.vector.line
import com.soywiz.korma.geom.vector.rect
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.components.Sprite
import com.soywiz.korgeFleks.components.Text
import com.soywiz.korgeFleks.utils.ImageAnimView
import com.soywiz.korgeFleks.utils.KorgeViewCache
import com.soywiz.korgeFleks.utils.random

/**
 * This system is updating the view objects for all [Drawable] entities.
 *
 * In case the [Drawable] entity is of type [Sprite] it takes the image configuration from
 * [Sprite] component to setup and control the sprite animations.
 */
class KorgeViewSystem(
    private val korgeViewCache: KorgeViewCache = inject("normalViewCache"),
    private val korgeDebugViewCache: KorgeViewCache = inject("debugViewCache")
) : IteratingSystem(
    family { all(Appearance).any(Appearance, SwitchLayerVisibility, SpecificLayer, PositionShape, Offset).none(ParallaxIntro) },
    interval = EachFrame
) {
    private var lastY: Double = 0.0

    override fun onTickEntity(entity: Entity) {
        val appearance = entity[Appearance]

        // TODO this can be re-written with help of SpecificLayer ???
        entity.getOrNull(SwitchLayerVisibility)?.let { visibility ->
            visibility.spriteLayers.forEach { layer ->
                layer.visible = if (layer.visible) {
                    // try to switch off
                    (0.0..1.0).random() > visibility.onVariance  // variance in switching value off - 1: every frame switching possible - 0: no switching at all
                } else {
                    // try to switch on again
                    (0.0..1.0).random() <= visibility.offVariance  // variance in switching value on again - 1: changed value switches back immediately - 0: changed value stays forever
                }
                (korgeViewCache[entity] as ImageAnimView).getLayer(layer.name)?.visible = layer.visible
            }
        }

        val offset: Point = if (entity hasNo Offset) Point()
        else Point(entity[Offset].x, entity[Offset].y)
        entity.getOrNull(OffsetByFrameIndex)?.let {
            // Get offset depending on current animation and frame index
            val currentFrameIndex = (korgeViewCache[it.entity] as ImageAnimView).currentFrameIndex
            val animationName = it.entity.getOrNull(Sprite)?.animationName ?: ""
            offset += it.list[animationName]?.get(currentFrameIndex) ?: error("KorgeViewSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
        }

        korgeViewCache[entity].let { view ->
            if (appearance.visible) {
                view.visible = true
                view.alpha = appearance.alpha
                appearance.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }

                if (entity has PositionShape) {
                    val positionShape = entity[PositionShape]
                    view.x = positionShape.x - offset.x
                    view.y = positionShape.y - offset.y
                }

//                println("[${entity.id}] Y: ${view.y} (Position: ${positionShape.y} delta: ${lastY - positionShape.y})")
//                lastY = positionShape.y

            } else {
                view.visible = false
            }
        }

        // Do debug drawing if component is configured for this entity
        entity.getOrNull(DebugInfo)?.let { debugInfo ->
            // TODO check for Keys to enable certain debug options

            val positionShape = entity[PositionShape]

            korgeDebugViewCache[entity].let { debugView ->
                debugView.xy(positionShape.x, positionShape.y)

                @OptIn(KorgeExperimental::class)
                (debugView as Container).renderableView {
                    val fontSize = 8.0
                    if (debugInfo.showName) {
                        ctx.drawText(
                            ctx.views?.debugBmpFont ?: error("KorgeViewSystem: Could not load debugBmpFont!"),
                            fontSize, debugInfo.name,
                            positionShape.x.toInt(),
                            positionShape.y.toInt() + 1,
                            colMul = Colors.RED,
                            filtering = false
                        )
                    }

                    ctx.useLineBatcher { lines ->
                        lines.drawVector(Colors.RED) {
                            // Draw pivot point
                            if (debugInfo.showPivotPoint) {
                                val centerX = globalX
                                val centerY = globalY
                                line(centerX, centerY - 5, centerX, centerY + 5)
                                line(centerX - 5, centerY, centerX + 5, centerY)
                            }
                            // Draw overall entity object size
                            if (debugInfo.showSizeBox) {
                                rect(Rectangle(globalX - offset.x, globalY - offset.y, positionShape.width, positionShape.height))
                            }
                        }

/* here as reference for future use ;-)
                        lines.drawVector(Colors.YELLOW) {
                            moveTo(localToGlobal(Point(0.0 - offset.x, 0.0 - offset.y)))
                            lineTo(localToGlobal(Point(0.0 - offset.x, positionShape.height - offset.y)))
                            lineTo(localToGlobal(Point(positionShape.width - offset.x, positionShape.height - offset.y)))
                            lineTo(localToGlobal(Point(positionShape.width - offset.x, 0.0 - offset.y)))
                            lineTo(localToGlobal(Point(0.0 - offset.x, 0.0 - offset.y)))
                            close()

                            val radius = 6.0 * ctx.views!!.windowToGlobalScaleAvg
                            circle(Point(20, 30), radius)
                        }
*/
                    }
                }
            }
        }
    }
}
