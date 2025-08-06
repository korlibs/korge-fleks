package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.fastForEachReverse
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.NinePatch.Companion.NinePatchComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.prefab.Prefab
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.render.*


/**
 * Creates a new [DebugRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver this */

class DebugRenderSystem(
    private val world: World,
    private val layerTag: RenderLayerTag
) : RenderSystem {
    private val family: Family = world.family {
        all(layerTag)
            .any(layerTag, PositionComponent, CollisionComponent, SpriteComponent, TextFieldComponent, NinePatchComponent, LevelMapComponent, GridComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")
    private val position: Position = staticPositionComponent {}
    private val gridPosition: Position = staticPositionComponent {}
    private val gridColPosition: Position = staticPositionComponent {}

    private val grid = GridPosition()

    override fun render(ctx: RenderContext) {
        val camera: Entity = world.getMainCameraOrNull() ?: return

        // Custom Render Code here
        ctx.useLineBatcher { batch ->
            family.forEach { entity ->


                if (entity has PositionComponent) {
                    // Take over entity position
                    position.init(entity[PositionComponent])

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

                    if (entity has CollisionComponent && entity has GridComponent && entity has DebugInfoTag.SPRITE_COLLISION_BOUNDS) {
                        val gridComponent = entity[GridComponent]
                        val collisionBox = assetStore.getCollisionData(entity[CollisionComponent].name)

                        // Take over entity grid position and convert to screen coordinates
                        gridPosition.x = gridComponent.x
                        gridPosition.y = gridComponent.y
                        gridPosition.run { world.convertToScreenCoordinates(camera) }

                        // Draw collision bounds
                        batch.drawVector(Colors.LIGHTBLUE) {
                            rect(
                                x = gridPosition.x + collisionBox.x.toFloat(),
                                y = gridPosition.y + collisionBox.y.toFloat(),
                                width = collisionBox.width,
                                height = collisionBox.height
                            )
                        }

                        val xrRight = gridComponent.xr + (collisionBox.x + collisionBox.width) / AppConfig.GRID_CELL_SIZE
                        val yrBottom = gridComponent.yr + (collisionBox.y + collisionBox.height) / AppConfig.GRID_CELL_SIZE
                        val xrLeft = gridComponent.xr + collisionBox.x / AppConfig.GRID_CELL_SIZE
                        val yrTop = gridComponent.yr + collisionBox.y / AppConfig.GRID_CELL_SIZE

                        fun drawGridPosition(cx: Int, cy: Int, xr: Float, yr: Float, color: RGBA) {
                            gridPosition.x = (cx + xr) * AppConfig.GRID_CELL_SIZE
                            gridPosition.y = (cy + yr) * AppConfig.GRID_CELL_SIZE
                            gridPosition.run { world.convertToScreenCoordinates(camera) }
                            drawPoint(batch, gridPosition.x, gridPosition.y, color)
                        }

//                        // Bottom edge of the collision box
//                        grid.setAndNormalizeX(gridComponent.cx, xrLeft)
//                        repeat( ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
//                            drawGridPosition(grid.cx + i, gridComponent.cy, xrLeft, yrBottom, Colors.RED)
//                        }
//                        // Top edge of the collision box
//                        grid.setAndNormalizeX(gridComponent.cx, xrLeft)
//                        repeat( ceil(collisionBox.width / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
//                            drawGridPosition(grid.cx + i, gridComponent.cy, xrLeft, yrTop, Colors.RED)
//                        }
//                        // Right edge of the collision box
//                        grid.setAndNormalizeY(gridComponent.cy, yrTop)
//                        repeat( ceil(collisionBox.height / AppConfig.GRID_CELL_SIZE).toInt()) { i ->
//                            drawGridPosition(gridComponent.cx, grid.cy + i, xrRight, yrBottom, Colors.RED)
//                        }
//                        // Left edge of the collision box

                        // Bottom right corner of the collision box
                        drawGridPosition(gridComponent.cx, gridComponent.cy, xrRight, yrBottom, Colors.GREEN)
                        // Bottom left corner of the collision box
                        drawGridPosition(gridComponent.cx, gridComponent.cy, xrLeft, yrBottom, Colors.GREEN)
                        // Top right corner of the collision box
                        drawGridPosition(gridComponent.cx, gridComponent.cy, xrRight, yrTop, Colors.GREEN)
                        // Top left corner of the collision box
                        drawGridPosition(gridComponent.cx, gridComponent.cy, xrLeft, yrTop, Colors.GREEN)
                    }

                    // Draw pivot point (zero-point for game object)
                    if (entity has DebugInfoTag.POSITION) {
                        drawPoint(batch, position.x + position.offsetX, position.y + position.offsetY, Colors.YELLOW)
                    }
                }

                // Draw grid position (used in collision system)
                if(entity has GridComponent && entity has DebugInfoTag.GRID_POSITION) {
                    val gridComponent = entity[GridComponent]
                    // Take over entity grid position and convert to screen coordinates
                    gridPosition.x = gridComponent.x
                    gridPosition.y = gridComponent.y
                    gridPosition.run { world.convertToScreenCoordinates(camera) }

                    batch.drawVector(Colors.GREEN) {
                        val x = gridPosition.x
                        val y = gridPosition.y
                        circle(korlibs.math.geom.Point(x, y), 2)
                        line(korlibs.math.geom.Point(x - 3, y), korlibs.math.geom.Point(x + 3, y))
                        line(korlibs.math.geom.Point(x, y - 3), korlibs.math.geom.Point(x, y + 3))
                    }
                }


                if (entity has LevelMapComponent && entity has DebugInfoTag.LEVEL_MAP_COLLISION_BOUNDS) {
                    val levelData = Prefab.levelData ?: return@forEach
                    val tileSize = levelData.tileSize

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
                    levelData.forEachCollisionTile(xStart, yStart, xTiles, yTiles) { collisionTile, px, py ->
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

    private fun drawPoint(batch: LineRenderBatcher, x: Float, y: Float, color: RGBA) {
        batch.drawVector(color) {
            circle(korlibs.math.geom.Point(x, y), 2)
            line(korlibs.math.geom.Point(x - 3, y), korlibs.math.geom.Point(x + 3, y))
            line(korlibs.math.geom.Point(x, y - 3), korlibs.math.geom.Point(x, y + 3))
        }
    }

    private fun drawPointInLevel(batch: LineRenderBatcher, position: Position, color: RGBA) {

    }
}
