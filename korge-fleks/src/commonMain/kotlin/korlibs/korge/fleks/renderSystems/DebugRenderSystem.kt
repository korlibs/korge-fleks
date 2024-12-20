package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.*
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.RenderLayerTag
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.math.geom.Point


/**
 * Creates a new [DebugRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver this */
inline fun Container.debugRenderSystem(viewPortSize: SizeInt, camera: Entity, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker DebugRenderSystem.() -> Unit = {}) =
    DebugRenderSystem(viewPortSize,camera, world, layerTag).addTo(this, callback)

class DebugRenderSystem(
    private val viewPortSize: SizeInt,
    private val camera: Entity,
    world: World,
    private val layerTag: RenderLayerTag
) : View() {
    private val family: Family = world.family {
        all(layerTag, PositionComponent)
            .any(PositionComponent, SpriteComponent, LayeredSpriteComponent, TextFieldComponent, NinePatchComponent)
    }

    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code here
        ctx.useLineBatcher { batch ->
            family.forEach { entity ->
                val (worldX, worldY, offsetX, offsetY) = entity[PositionComponent]

                // Transform world coordinates to screen coordinates
                val cameraPosition = camera[PositionComponent]
                val x = - worldX + cameraPosition.x + (viewPortSize.width * 0.5f)
                val y = - worldY + cameraPosition.y + (viewPortSize.height * 0.5f)

                val xx: Float = x + offsetX
                val yy: Float = y + offsetY

                // In case the entity is a sprite than render the overall sprite size and the texture bounding boxes
                if (entity has SpriteComponent) {
                    val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                    val imageFrame = assetStore.getImageFrame(name, animation, frameIndex)
                    val imageData = assetStore.getImageData(name)

                    // Draw sprite bounds
                    batch.drawVector(Colors.RED) {
                        rect(
                            x = xx - anchorX,
                            y = yy - anchorY,
                            width = imageData.width.toFloat(),
                            height = imageData.height.toFloat()
                        )
                    }
                    // Draw texture bounds for each layer
                    imageFrame.layerData.fastForEachReverse { layer ->
                        batch.drawVector(Colors.GREEN) {
                            rect(
                                x = xx + layer.targetX.toFloat() - anchorX,
                                y = yy + layer.targetY.toFloat() - anchorY,
                                width = layer.width.toFloat(),
                                height = layer.height.toFloat()
                            )
                        }
                    }
                }

                if (entity has TextFieldComponent) {
                    // Draw text field bounds
                    batch.drawVector(Colors.RED) {
                        val (_, _, _, _, width, height) = entity[TextFieldComponent]
                        rect(
                            x = xx,
                            y = yy,
                            width = width,
                            height = height
                        )
                    }
                }

                if (entity has NinePatchComponent) {
                    // Draw nine patch bounds
                    batch.drawVector(Colors.RED) {
                        val (_, width, height) = entity[NinePatchComponent]
                        rect(
                            x = xx,
                            y = yy,
                            width = width,
                            height = height
                        )
                    }
                }

                // Draw pivot point (zero-point for game object)
                batch.drawVector(Colors.YELLOW) {
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
    }
}
