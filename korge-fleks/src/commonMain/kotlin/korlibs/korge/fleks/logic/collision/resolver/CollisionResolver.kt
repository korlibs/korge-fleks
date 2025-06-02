package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.logic.collision.checker.CollisionChecker

/**
 * A base class for resolving collisions in a grid-based system.
 */
abstract class CollisionResolver {

    open fun resolveXCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collision: CollisionChecker,
        dir: Int
    ) = Unit

    open fun resolveYCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collision: CollisionChecker,
        dir: Int
    ) = Unit
}
