package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
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
            .any(PositionComponent, SpriteComponent, LayeredSpriteComponent, TextFieldComponent, NinePatchComponent, LevelMapComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()

        // Custom Render Code here
        ctx.useLineBatcher { batch ->
            family.forEach { entity ->


                if (entity has PositionComponent) {
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
//                        // Draw texture bounds for each layer
//                        imageFrame.layerData.fastForEachReverse { layer ->
//                            batch.drawVector(Colors.GREEN) {
//                                rect(
//                                    x = position.x + position.offsetX + layer.targetX.toFloat() - anchorX,
//                                    y = position.y + position.offsetY + layer.targetY.toFloat() - anchorY,
//                                    width = layer.width.toFloat(),
//                                    height = layer.height.toFloat()
//                                )
//                            }
//                        }
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

                    if (entity has CollisionComponent) {
                        val (anchorX, anchorY, colWidth, colHeight) = assetStore.getCollisionData(entity[CollisionComponent].configName.toString())
                        // Draw collision bounds
                        batch.drawVector(Colors.LIGHTBLUE) {
                            rect(
                                x = position.x + position.offsetX - anchorX.toFloat(),
                                y = position.y + position.offsetY - anchorY.toFloat(),
                                width = colWidth.toFloat(),
                                height = colHeight.toFloat()
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

                if (entity has LevelMapComponent) {
                    val levelName = entity[LevelMapComponent].levelName
                    val worldData = assetStore.getWorldData(levelName)
                    val tileSize = worldData.tileSize

                    val cameraPosition = with(world) { camera[PositionComponent] }

                    // Calculate viewport position in world coordinates from Camera position (x,y) + offset
                    val viewPortPosX: Float = cameraPosition.x + cameraPosition.offsetX - AppConfig.VIEW_PORT_WIDTH_HALF
                    val viewPortPosY: Float = cameraPosition.y + cameraPosition.offsetY - AppConfig.VIEW_PORT_HEIGHT_HALF

                    // Start and end indexes of viewport area (in tile coordinates)
                    val xStart: Int = viewPortPosX.toInt() / tileSize - 1  // x in positive direction;  -1 = start one tile before
                    val xTiles = (AppConfig.VIEW_PORT_WIDTH / tileSize) + 3

                    val yStart: Int = viewPortPosY.toInt() / tileSize - 1  // y in negative direction;  -1 = start one tile before
                    val yTiles = (AppConfig.VIEW_PORT_HEIGHT / tileSize) + 3

                    // Draw collision tiles
                    worldData.forEachCollisionTile(xStart, yStart, xTiles, yTiles) { collisionTile, px, py ->
                        if (collisionTile == 1) {
                            batch.drawVector(Colors.RED) {
                                rect(px - viewPortPosX, py - viewPortPosY, tileSize.toFloat(), tileSize.toFloat())
                            }
                        }
                    }
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
