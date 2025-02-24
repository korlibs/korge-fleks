package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.*
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*
import korlibs.math.geom.Point


/**
 * Creates a new [DebugRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver this */
inline fun Container.debugRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker DebugRenderSystem.() -> Unit = {}) =
    DebugRenderSystem(world, layerTag).addTo(this, callback)

class DebugRenderSystem(
    private val world: World,
    private val layerTag: RenderLayerTag
) : View() {
    private val family: Family = world.family {
        all(layerTag)
            .any(PositionComponent, SpriteComponent, LayeredSpriteComponent, TextFieldComponent, NinePatchComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()

        // Custom Render Code here
        ctx.useLineBatcher { batch ->
            family.forEach { entity ->
                val entityPosition = entity[PositionComponent]

                val position: PositionComponent = if (entity has ScreenCoordinatesTag) {
                    // Take over entity coordinates
                    entityPosition
                } else {
                    // Transform world coordinates to screen coordinates
                    entityPosition.run { world.convertToScreenCoordinates(camera) }
                }

                // In case the entity is a sprite than render the overall sprite size and the texture bounding boxes
                if (entity has SpriteComponent) {
                    val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                    val imageFrame = assetStore.getImageFrame(name, animation, frameIndex)
                    val imageData = assetStore.getImageData(name)

                    // Draw sprite bounds
                    batch.drawVector(Colors.RED) {
                        rect(
                            x = position.x + position.offsetX - anchorX,
                            y = position.y + position.offsetY - anchorY,
                            width = imageData.width.toFloat(),
                            height = imageData.height.toFloat()
                        )
                    }
                    // Draw texture bounds for each layer
                    imageFrame.layerData.fastForEachReverse { layer ->
                        batch.drawVector(Colors.GREEN) {
                            rect(
                                x = position.x + position.offsetX + layer.targetX.toFloat() - anchorX,
                                y = position.y + position.offsetY + layer.targetY.toFloat() - anchorY,
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
                            x = position.x + position.offsetX,
                            y = position.y + position.offsetY,
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
                            x = position.x + position.offsetX,
                            y = position.y + position.offsetY,
                            width = width,
                            height = height
                        )
                    }
                }

                // Draw pivot point (zero-point for game object)
                batch.drawVector(Colors.YELLOW) {
                    val x = position.x + position.offsetX
                    val y = position.y + position.offsetY
                    circle(Point(x, y), 2)
                    line(Point(x - 3, y), Point(x + 3, y))
                    line(Point(x, y - 3), Point(x, y + 3))
                }
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle = with (world) {
        return Rectangle(0, 0, AppConfig.VIEW_PORT_WIDTH, AppConfig.VIEW_PORT_HEIGHT)
    }

    init {
        name = layerTag.toString()
    }
}
