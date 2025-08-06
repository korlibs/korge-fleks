package korlibs.korge.fleks.logic.collision.resolver

import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Grid
import korlibs.korge.fleks.components.Motion


/**
 * A base class for resolving collisions in a grid-based system.
 */
abstract class CollisionResolver {

    /**
     * Resolves the X collision for an entity.
     *
     * This method can be overridden to provide custom collision resolution logic for the X direction like:
     * - Adjusting the entity's position based on the collision direction.
     * - Modifying the entity's velocity based on the collision.
     * - Triggering any necessary events or actions based on the collision.
     *
     * @param gridComponent The grid component of the entity.
     * @param motionComponent The motion component of the entity.
     * @param collisionBox The collision box with offset and size.
     * @param dir The direction of the collision (1 for right, -1 for left).
     */
    open fun resolveXCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) = Unit

    /**
     * Resolves the Y collision for an entity.
     *
     * This method can be overridden to provide custom collision resolution logic for the Y direction like:
     * - Adjusting the entity's position based on the collision direction.
     * - Modifying the entity's velocity based on the collision.
     * - Triggering any necessary events or actions based on the collision.
     *
     * @param gridComponent The grid component of the entity.
     * @param motionComponent The motion component of the entity.
     * @param collisionBox The collision box with offset and size.
     * @param dir The direction of the collision (1 for down, -1 for up).
     */
    open fun resolveYCollision(
        gridComponent: Grid,
        motionComponent: Motion,
        collisionBox: AssetStore.CollisionData,
        dir: Int
    ) = Unit
}
