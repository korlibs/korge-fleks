package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.fastForEachReverse
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.LayeredSprite.Companion.LayeredSpriteComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.NinePatch.Companion.NinePatchComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.components.getImageFrame
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
    private val position: Position = staticPositionComponent()


    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()

        // Custom Render Code here
        ctx.useLineBatcher { batch ->
            family.forEach { entity ->


                if (entity has PositionComponent) {
                    // Take over entity position
                    position.init(entity[PositionComponent])

                    // TODO: Check if this works...
                    if (entity hasNo ScreenCoordinatesTag) {
                        // Transform world coordinates to screen coordinates
                        position.run { world.convertToScreenCoordinates(camera) }
                    }

                    // In case the entity is a sprite than render the overall sprite size and the texture bounding boxes
                    if (entity has SpriteComponent) {
                        val spriteComponent = entity[SpriteComponent]
                        val imageFrame = assetStore.getImageFrame(spriteComponent.name, spriteComponent.animation, spriteComponent.frameIndex)
                        val imageData = assetStore.getImageData(spriteComponent.name)

                        // Draw sprite bounds
                        if (entity has DebugInfoTag.SPRITE_BOUNDS) {
                            batch.drawVector(Colors.RED) {
                                rect(
                                    x = position.x + position.offsetX - spriteComponent.anchorX,
                                    y = position.y + position.offsetY - spriteComponent.anchorY,
                                    width = imageData.width.toFloat(),
                                    height = imageData.height.toFloat()
                                )
                            }
                        }
                        // Draw texture bounds for each layer
                        if (entity has DebugInfoTag.SPRITE_TEXTURE_BOUNDS) {
                            imageFrame.layerData.fastForEachReverse { layer ->
                                batch.drawVector(Colors.GREEN) {
                                    rect(
                                        x = position.x + position.offsetX + layer.targetX.toFloat() - spriteComponent.anchorX,
                                        y = position.y + position.offsetY + layer.targetY.toFloat() - spriteComponent.anchorY,
                                        width = layer.width.toFloat(),
                                        height = layer.height.toFloat()
                                    )
                                }
                            }
                        }
                    }

                    if (entity has TextFieldComponent && entity has DebugInfoTag.TEXT_FIELD_BOUNDS) {
                        // Draw text field bounds
                        batch.drawVector(Colors.RED) {
                            val textFieldComponent = entity[TextFieldComponent]
                            rect(
                                x = position.x + position.offsetX,
                                y = position.y + position.offsetY,
                                width = textFieldComponent.width,
                                height = textFieldComponent.height
                            )
                        }
                    }

                    if (entity has NinePatchComponent && entity has DebugInfoTag.NINE_PATCH_BOUNDS) {
                        // Draw nine patch bounds
                        batch.drawVector(Colors.RED) {
                            val ninePatchComponent = entity[NinePatchComponent]
                            rect(
                                x = position.x + position.offsetX,
                                y = position.y + position.offsetY,
                                width = ninePatchComponent.width,
                                height = ninePatchComponent.height
                            )
                        }
                    }

                    if (entity has CollisionComponent && entity has DebugInfoTag.SPRITE_COLLISION_BOUNDS) {
                        val (anchorX, anchorY, colWidth, colHeight) = assetStore.getCollisionData(entity[CollisionComponent].configName)
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
                    if (entity has DebugInfoTag.POSITION) {
                        batch.drawVector(Colors.YELLOW) {
                            val x = position.x + position.offsetX
                            val y = position.y + position.offsetY
                            circle(Point(x, y), 2)
                            line(Point(x - 3, y), Point(x + 3, y))
                            line(Point(x, y - 3), Point(x, y + 3))
                        }
                    }
                }

                if (entity has LevelMapComponent && entity has DebugInfoTag.LEVEL_MAP_COLLISION_BOUNDS) {
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
                        if (collisionTile == 4) {
                            batch.drawVector(Colors.YELLOW) {
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
