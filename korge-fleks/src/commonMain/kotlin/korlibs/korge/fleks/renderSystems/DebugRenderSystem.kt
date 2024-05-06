package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.*
import korlibs.image.color.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.RenderLayerTag
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.math.geom.Point


/**
 * Creates a new [DebugRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver this */
inline fun Container.debugRenderSystem(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker DebugRenderSystem.() -> Unit = {}) =
    DebugRenderSystem(viewPortSize, world, layerTag).addTo(this, callback)

class DebugRenderSystem(
    private val viewPortSize: SizeInt,
    world: World,
    private val layerTag: RenderLayerTag
) : View() {
    private val family: Family

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code here
        ctx.useLineBatcher { batch ->
            family.forEach { entity ->
                val (x, y) = entity[PositionComponent]
                val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                val imageFrame = AssetStore.getImageFrame(name, animation, frameIndex)
                val imageData = AssetStore.getImageData(name)

                // Draw sprite bounds
                batch.drawVector(Colors.RED) {
                    rect(
                        x = x - anchorX,
                        y = y - anchorY,
                        width = imageData.width.toFloat(),
                        height = imageData.height.toFloat()
                    )
                }
                // Draw texture bounds for each layer
                imageFrame.layerData.fastForEachReverse { layer ->
                    batch.drawVector(Colors.GREEN) {
                        rect(
                            x = x + layer.targetX.toFloat() - anchorX,
                            y = y + layer.targetY.toFloat() - anchorY,
                            width = layer.width.toFloat(),
                            height = layer.height.toFloat()
                        )
                    }
                }
                // Draw pivot point (zero-point for game object)
                batch.drawVector(Colors.YELLOW) {
                    val xx: Float = x
                    val yy: Float = y
                    circle(Point(xx, yy), 2)
                    line(Point(xx - 3, yy), Point(xx + 3, yy))
                    line(Point(xx, yy - 3), Point(xx, yy + 3))
                }
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle =
        Rectangle(0, 0, viewPortSize.width, viewPortSize.height)

    init {
        name = layerTag.toString()
        family = world.family { all(layerTag, SpriteComponent, PositionComponent)}
    }
}
