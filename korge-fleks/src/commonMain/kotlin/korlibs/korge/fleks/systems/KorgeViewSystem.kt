package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.github.quillraven.fleks.World.Companion.inject
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.annotations.KorgeExperimental
import korlibs.korge.bitmapfont.drawText
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.Sprite
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.fleks.utils.random
import korlibs.korge.parallax.ImageDataViewEx
import korlibs.korge.parallax.ParallaxDataView
import korlibs.korge.render.useLineBatcher
import korlibs.korge.view.*
import korlibs.math.interpolation.Easing
import korlibs.time.TimeSpan

/**
 * This system is updating the view objects for all [Drawable] entities.
 *
 * In case the [Drawable] entity is of type [Sprite] it takes the image configuration from
 * [Sprite] component to setup and control the sprite animations.
 */
class KorgeViewSystem(
    private val korgeViewCache: KorgeViewCache = inject("KorgeViewCache"),
    private val korgeViewCacheDebug: KorgeViewCache = inject("KorgeViewCacheDebug")
) : IteratingSystem(
    family { all(Appearance).any(Appearance, SwitchLayerVisibility, SpecificLayer, PositionShape, Offset) },
    interval = EachFrame
) {
    var updateViewsEnabled: Boolean = true
    private var lastY: Double = 0.0

    private fun updateAnimateComponent(entity: Entity, componentProperty: AnimateComponentType, value: Any, change: Any = Unit, duration: Float? = null, easing: Easing? = null) {
        entity.configure { animatedEntity ->
            animatedEntity.getOrAdd(componentProperty.type) { AnimateComponent(componentProperty) }.also {
                it.change = change
                it.value = value
                it.duration = duration ?: 0f
                it.timeProgress = 0f
                it.easing = easing ?: Easing.LINEAR
            }
        }
    }
    override fun onTickEntity(entity: Entity) {
        val appearance = entity[Appearance]

        if (updateViewsEnabled) {
            // TODO this can be re-written with help of SpecificLayer ???
            entity.getOrNull(SwitchLayerVisibility)?.let { visibility ->
                visibility.spriteLayers.forEach { layer ->
                    layer.visible = if (layer.visible) {
                        // try to switch off
                        (0f..1f).random() > visibility.offVariance  // variance in switching value off - 1: every frame switching possible - 0: no switching at all
                    } else {
                        // try to switch on again
                        (0f..1f).random() <= visibility.onVariance  // variance in switching value on again - 1: changed value switches back immediately - 0: changed value stays forever
                    }
                    (korgeViewCache[entity] as ImageDataViewEx).getLayer(layer.name)?.visible = layer.visible
                }
            }
        }

        val offset: Point = if (entity hasNo Offset) Point() else Point(entity[Offset].x, entity[Offset].y)
        entity.getOrNull(OffsetByFrameIndex)?.let {
            // Get offset depending on current animation and frame index
            val currentFrameIndex = (korgeViewCache[it.entity] as ImageDataViewEx).currentFrameIndex
            val animationName = it.entity.getOrNull(Sprite)?.animationName ?: ""
            val frameOffset = it.list[animationName]?.get(currentFrameIndex)
                ?: error("KorgeViewSystem: Cannot get offset by frame index (entity: ${entity.id}, animationName: '$animationName', currentFrameIndex: $currentFrameIndex)")
            offset.x += frameOffset.x
            offset.y += frameOffset.y
        }

        // TODO put into own system ???
        entity.getOrNull(ChangeOffsetRandomly)?.let { changeOffsetRandomly ->

            val random: Float = (0f .. 1f).random()
            if (!changeOffsetRandomly.triggered && random > changeOffsetRandomly.triggerChangeVariance) {
                // Create new random variance value for adding to offset
                val startX = changeOffsetRandomly.x
                val startY = changeOffsetRandomly.y
                val endX = changeOffsetRandomly.offsetXRange * (-1f .. 1f).random()
                val endY = changeOffsetRandomly.offsetYRange * (-1f .. 1f).random()
                updateAnimateComponent(entity, AnimateComponentType.ChangeOffsetRandomlyX, value = startX, change = endX - startX, 3f, null)
                updateAnimateComponent(entity, AnimateComponentType.ChangeOffsetRandomlyY, value = startY, change = endY - startY, 3f, null)
                changeOffsetRandomly.triggered = true
            } else if (changeOffsetRandomly.triggered && random <= changeOffsetRandomly.triggerBackVariance) {
                // Reset and enable switching to new random value again
                changeOffsetRandomly.triggered = false
            }

            offset.x += changeOffsetRandomly.x
            offset.y += changeOffsetRandomly.y
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

            if (updateViewsEnabled) {
                if (view is ImageDataViewEx) view.update(TimeSpan(deltaTime.toDouble() * 1000.0))
                else if (view is ParallaxDataView) view.update(TimeSpan(deltaTime.toDouble() * 1000.0))
            }

        }

        // Do debug drawing if component is configured for this entity
        entity.getOrNull(Info)?.let { debugInfo ->
            // TODO check for Keys to enable certain debug options
            if (!debugInfo.showName) return

            val positionShape = entity[PositionShape]

            korgeViewCacheDebug[entity].let { debugView ->
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
// TODO                                val centerX = globalX
//                                val centerY = globalY
//                                line(centerX, centerY - 5, centerX, centerY + 5)
//                                line(centerX - 5, centerY, centerX + 5, centerY)
                            }
                            // Draw overall entity object size
                            if (debugInfo.showSizeBox) {
// TODO                                rect(Rectangle(globalX - offset.x, globalY - offset.y, positionShape.width, positionShape.height))
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
