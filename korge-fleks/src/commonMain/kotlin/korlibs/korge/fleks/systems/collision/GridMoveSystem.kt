package korlibs.korge.fleks.systems.collision

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Collision.Companion.CollisionComponent
import korlibs.korge.fleks.components.Gravity.Companion.GravityComponent
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Grid.Companion.GridComponent
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.gridCollisionXComponent
import korlibs.korge.fleks.logic.collision.GridPosition
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker
import korlibs.korge.fleks.logic.collision.checker.PlatformerCollisionChecker
import korlibs.korge.fleks.logic.collision.resolver.CollisionResolver
import korlibs.korge.fleks.logic.collision.resolver.SimpleCollisionResolver
import korlibs.korge.fleks.utils.AppConfig
import korlibs.math.isAlmostEquals
import kotlin.compareTo
import kotlin.math.abs
import kotlin.math.ceil

/*
data class CollisionGrid(
    val cxTopLeft: Int,  // Cell index
    val cyTopLeft: Int,
    val xrTopLeft: Float,  // "Relative" position in the cell
    val yrTopLeft: Float,

    val cxBottomLeft: Int,
    val cyBottomLeft: Int,
    val xrBottomLeft: Float,
    val yrBottomLeft: Float,

    val cxTopRight: Int,
    val cyTopRight: Int,
    val xrTopRight: Float,
    val yrTopRight: Float,

    val cxBottomRight: Int,
    val cyBottomRight: Int,
    val xrBottomRight: Float,
    val yrBottomRight: Float
)
*/

class GridMoveSystem(
) : IteratingSystem(
    family = World.family { all(PositionComponent, MotionComponent, CollisionComponent) /*.any(LevelMapComponent)*/ },
    interval = Fixed(1 / 30f)
) {
    val assetStore = world.inject<AssetStore>("AssetStore")

    var collisionChecker: CollisionChecker = PlatformerCollisionChecker()
    var collisionResolver: CollisionResolver = SimpleCollisionResolver(16, 16)

    private val grid = GridPosition()

    override fun onTickEntity(entity: Entity) {
        // Iterate over all entities that have a GridComponent and MotionComponent
        val positionComponent = entity[PositionComponent]
        val motionComponent = entity[MotionComponent]
//        val gridComponent = entity[GridComponent]
        val collisionComponent = entity[CollisionComponent]
        val assetStore = world.inject<AssetStore>(name = "AssetStore")
        val (anchorX, anchorY, colWidth, colHeight) = assetStore.getCollisionData(collisionComponent.name)
        val gravityComponent = entity.getOrNull(GravityComponent)
//        gridComponent.lastPx = gridComponent.attachX
//        gridComponent.lastPy = gridComponent.attachY
        if (gravityComponent != null) {
// TODO enable gravity again
//            motionComponent.velocityX += gravityComponent.calculateDeltaXGravity()
//            motionComponent.velocityY += gravityComponent.calculateDeltaYGravity()
        }

        grid.x = positionComponent.x
        grid.y = positionComponent.y

        /**
         * Any movement greater than [Grid.maxGridMovementPercent] will increase the number of steps here.
         * The steps will break down the movement into smaller iterators to avoid jumping over grid collisions
         */
//        val steps = ceil(abs(motionComponent.velocityX) + abs(motionComponent.velocityY) / AppConfig.maxGridMovementPercent)
        val overallMovement = motionComponent.velocityX * deltaTime
        val steps = ceil(abs(overallMovement) / AppConfig.GRID_CELL_SIZE)  // / AppConfig.maxGridMovementPercent)
        if (steps > 0) {
            // First we need to calculate the top left point of the collision rectangle
            grid.applyOnX(anchorX)
//            grid.applyOnY(anchorY)

            var i = 0
            while (i < steps) {
                // Move the entity in the X direction
//                grid.applyOnX(overallMovement / steps)
                grid.xr += overallMovement / steps / AppConfig.GRID_CELL_SIZE

/*
                val collisionGrid = CollisionGrid(
                    cxTopLeft = ((positionComponent.x + positionComponent.offsetX + anchorX) / AppConfig.GRID_CELL_SIZE).toInt(),
                    cyTopLeft = ((positionComponent.y + positionComponent.offsetY + anchorY) / AppConfig.GRID_CELL_SIZE).toInt(),
                    xrTopLeft = ((positionComponent.x + positionComponent.offsetX + anchorX).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,
                    yrTopLeft = ((positionComponent.y + positionComponent.offsetY + anchorY).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,

                    cxBottomLeft = ((positionComponent.x + positionComponent.offsetX + anchorX) / AppConfig.GRID_CELL_SIZE).toInt(),
                    cyBottomLeft = ((positionComponent.y + positionComponent.offsetY + anchorY + colHeight) / AppConfig.GRID_CELL_SIZE).toInt(),
                    xrBottomLeft = ((positionComponent.x + positionComponent.offsetX + anchorX).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,
                    yrBottomLeft = ((positionComponent.y + positionComponent.offsetY + anchorY + colHeight).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,

                    cxTopRight = ((positionComponent.x + positionComponent.offsetX + anchorX + colWidth) / AppConfig.GRID_CELL_SIZE).toInt(),
                    cyTopRight = ((positionComponent.y + positionComponent.offsetY + anchorY) / AppConfig.GRID_CELL_SIZE).toInt(),
                    xrTopRight = ((positionComponent.x + positionComponent.offsetX + anchorX + colWidth).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,
                    yrTopRight = ((positionComponent.y + positionComponent.offsetY + anchorY).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,

                    cxBottomRight = ((positionComponent.x + positionComponent.offsetX + anchorX + colWidth) / AppConfig.GRID_CELL_SIZE).toInt(),
                    cyBottomRight = ((positionComponent.y + positionComponent.offsetY + anchorY + colHeight) / AppConfig.GRID_CELL_SIZE).toInt(),
                    xrBottomRight = ((positionComponent.x + positionComponent.offsetX + anchorX + colWidth).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,
                    yrBottomRight = ((positionComponent.y + positionComponent.offsetY + anchorY + colHeight).toInt() % AppConfig.GRID_CELL_SIZE) / AppConfig.GRID_CELL_SIZE,
                )
*/
                if (motionComponent.velocityX != 0f) {

//                    collisionChecker.preXCheck(
//                        grid.cx,
//                        grid.cy,
//                        grid.xr,
//                        grid.yr,
//                        motionComponent.velocityX,
//                        motionComponent.velocityY,
//                        colWidth,
//                        colHeight,
//                        AppConfig.GRID_CELL_SIZE
//                    )

                    // Check if collision happens within the next grid cell where the entity is moving
                    val result = collisionChecker.checkXCollision(
                        grid.cx,  // Top left of collision rectangle
                        grid.cy,
                        grid.xr,
                        grid.yr,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
                        colWidth,
                        colHeight,
                        AppConfig.GRID_CELL_SIZE
                    )
                    if (result != 0) {
//                        collisionResolver.resolveXCollision(grid, motionComponent, collisionChecker, result)
                        collisionResolver.resolveXCollision(grid, motionComponent, colWidth, colHeight, result)
                        entity.configure {
                            it += gridCollisionXComponent {
                                dir = result
                            }
                        }
                    }
                }
                // Adjust the xr value to ensure it stays within the grid cell bounds
                while (grid.xr > 1) {
                    grid.xr--
                    grid.cx++
                }
                while (grid.xr < 0) {
                    grid.xr++
                    grid.cx--
                }
/*
                // Move the entity in the Y direction
                gridComponent.yr += motionComponent.velocityY / steps
                if (motionComponent.velocityY != 0f) {
                    collisionChecker.preYCheck(
                        collisionGrid,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
//                        colWidth,
//                        colHeight,
//                        AppConfig.GRID_CELL_SIZE
                    )
                    val result = collisionChecker.checkYCollision(
                        collisionGrid,
                        motionComponent.velocityX,
                        motionComponent.velocityY,
//                        colWidth,
//                        colHeight,
//                        AppConfig.GRID_CELL_SIZE
                    )
                    if (result != 0) {
                        collisionResolver.resolveYCollision(gridComponent, motionComponent, collisionChecker, result)
                        entity.configure {
                            it += gridCollisionYComponent {
                                dir = result
                            }
                        }
                    }
                }
                while (gridComponent.yr > 1) {
                    gridComponent.yr--
                    gridComponent.cy++
                }
                while (gridComponent.yr < 0) {
                    gridComponent.yr++
                    gridComponent.cy--
                }
*/
                i++
            }

            // After all steps are done, we can set the final position of the entity
            grid.applyOnX(-anchorX)
//            grid.applyOnY(-anchorY)
            positionComponent.x = grid.x
            positionComponent.y = grid.y
        }
        motionComponent.velocityX *= motionComponent.frictionX
        if (motionComponent.velocityX.isAlmostEquals(0.0005f)) {
            motionComponent.velocityX = 0f
        }
        motionComponent.velocityY *= motionComponent.frictionY
        if (motionComponent.velocityY.isAlmostEquals(0.0005f)) {
            motionComponent.velocityY = 0f
        }
//        gridComponent.zr += motionComponent.velocityZ
//        if (gridComponent.zr > 0 && gravityComponent != null) {
//            motionComponent.velocityZ -= gravityComponent.calculateDeltaZGravity()
//        }
//        if (gridComponent.zr < 0) {
//            gridComponent.zr = 0f
//            motionComponent.velocityZ = 0f
//            entity.configure {
//                it += gridCollisionZComponent {
//                    dir = 0
//                }
//            }
//        }
//        motionComponent.velocityZ *= motionComponent.frictionZ
//        if (motionComponent.velocityZ.isAlmostEquals(0.0005f)) {
//            motionComponent.velocityZ = 0f
//        }

//        positionComponent.x = grid.x
//        positionComponent.y = grid.y
//        println("GridMoveSystem: cx, cy: ${gridComponent.cx}, ${gridComponent.cy} xr, yr: ${gridComponent.xr}, ${gridComponent.yr}")
    }

    override fun onAlphaEntity(entity: Entity, alpha: Float) {
        entity[GridComponent].interpolationAlpha = alpha
    }
}
