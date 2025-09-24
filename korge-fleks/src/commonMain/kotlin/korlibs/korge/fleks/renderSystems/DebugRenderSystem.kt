package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.DebugCollisionShapes.Companion.DebugCollisionShapesComponent
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.NinePatch.Companion.NinePatchComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
import korlibs.korge.fleks.components.State.Companion.StateComponent
import korlibs.korge.fleks.components.TextField.Companion.TextFieldComponent
import korlibs.korge.fleks.components.data.Point
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
            .any(layerTag, PositionComponent, CollisionComponent, SpriteComponent, TextFieldComponent,
                NinePatchComponent, LevelMapComponent, GridComponent, DebugCollisionShapesComponent)
    }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")
    private val position: Position = staticPositionComponent {}
    private val gridPosition: Position = staticPositionComponent {}
    private val grid2Position: Position = staticPositionComponent {}
    private var camera: Entity = Entity.NONE

    private val grid = GridPosition()
//    private val debugPointPool = world.inject<DebugPointPool>("DebugPointPool")

    override fun render(ctx: RenderContext) {
        camera = world.getMainCameraOrNull() ?: return

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
                        val sprite = assetStore.getTextureSprite(spriteComponent.name)
                        val texture = sprite[spriteComponent.frameIndex]

                        // Draw sprite bounds
                        if (entity has DebugInfoTag.SPRITE_BOUNDS) {
                            batch.drawVector(Colors.RED) {
                                rect(
                                    x = position.x + position.offsetX - spriteComponent.anchorX,
                                    y = position.y + position.offsetY - spriteComponent.anchorY,
                                    width = sprite.width.toFloat(),
                                    height = sprite.height.toFloat()
                                )
                            }
                        }
                        // Draw texture bounds for each layer
                        if (entity has DebugInfoTag.SPRITE_TEXTURE_BOUNDS) {
                            batch.drawVector(Colors.GREEN) {
                                rect(
                                    x = position.x + position.offsetX + (if (spriteComponent.flipX) (sprite.width - texture.targetX - texture.bmpSlice.width) else texture.targetX) - spriteComponent.anchorX,
                                    y = position.y + position.offsetY + (if (spriteComponent.flipY) (sprite.height - texture.targetY - texture.bmpSlice.height) else texture.targetY) - spriteComponent.anchorY,
                                    width = texture.bmpSlice.width.toFloat(),
                                    height = texture.bmpSlice.height.toFloat()
                                )
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

                    if (entity has DebugInfoTag.NINE_PATCH_BOUNDS && entity has NinePatchComponent) {
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

                    if (entity has DebugInfoTag.COLLISION_BOX && entity has CollisionComponent && entity has GridComponent) {
                        val gridComponent = entity[GridComponent]
                        val stateComponent = entity[StateComponent]
                        val gameObjectStateConfig = assetStore.getGameObjectStateConfig(stateComponent.name)
                        val collisionBox = gameObjectStateConfig.getCollisionData(stateComponent.current)

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
                    }

                    if (entity has DebugInfoTag.COLLISION_CELL_AND_RATIO_POINTS && entity has DebugCollisionShapesComponent) {
                        val debugCollisionShapesComponent = entity[DebugCollisionShapesComponent]
                        debugCollisionShapesComponent.gridCells.forEach { cell -> drawGridCell(batch, cell, Colors.GREEN) }
                        debugCollisionShapesComponent.ratioPositions.forEach { point -> drawRatioPoint(batch, point, Colors.RED) }
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

    private fun drawGridCell(batch: LineRenderBatcher, cell: Point, color: RGBA) {
        gridPosition.x = cell.x
        gridPosition.y = cell.y
        gridPosition.run { world.convertToScreenCoordinates(camera) }
        batch.drawVector(color) {
            rect(gridPosition.x, gridPosition.y, AppConfig.GRID_CELL_SIZE, AppConfig.GRID_CELL_SIZE)
        }
    }

    private fun drawRatioPoint(batch: LineRenderBatcher, point: Point, color: RGBA) {
        gridPosition.x = point.x
        gridPosition.y = point.y
        gridPosition.run { world.convertToScreenCoordinates(camera) }
        drawPoint(batch, gridPosition.x, gridPosition.y, color)
    }
}
