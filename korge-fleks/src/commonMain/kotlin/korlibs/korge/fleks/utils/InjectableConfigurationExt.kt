package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Collision.Companion.addCollisionComponentPool
import korlibs.korge.fleks.components.Gravity.Companion.addGravityComponentPool
import korlibs.korge.fleks.components.Grid.Companion.addGridComponentPool
import korlibs.korge.fleks.components.LevelMap.Companion.addLevelMapComponentPool
import korlibs.korge.fleks.components.PlatformerCollision.Companion.addPlatformerComponentPool
import korlibs.korge.fleks.components.PlayerInput.Companion.addPlayerInputComponentPool
import korlibs.korge.fleks.components.collision.GridCollisionResult.Companion.addGridCollisionResultComponentPools
import korlibs.korge.fleks.components.data.Point.Companion.addPointDataPool

/**
 * Add here all component pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksComponentPools(preAllocate: Int = 0) {
    addCollisionComponentPool(preAllocate)
    addGravityComponentPool(preAllocate)
    addGridCollisionResultComponentPools(preAllocate)
    addGridComponentPool(preAllocate)
    addLevelMapComponentPool(preAllocate)
    addPlatformerComponentPool(preAllocate)
    addPlayerInputComponentPool(preAllocate)
}

/**
 * Add here all data pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksDataPools(preAllocate: Int = 0) {
    addPointDataPool(preAllocate)
}