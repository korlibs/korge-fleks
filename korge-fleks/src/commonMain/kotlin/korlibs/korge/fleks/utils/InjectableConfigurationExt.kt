package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Collision.Companion.addCollisionComponentPool
import korlibs.korge.fleks.components.Info.Companion.addInfoComponentPool
import korlibs.korge.fleks.components.LevelMap.Companion.addLevelMapComponentPool
import korlibs.korge.fleks.components.data.Point.Companion.addPointDataPool

/**
 * Add here all component pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksComponentPools(preAllocate: Int = 0) {
    addCollisionComponentPool(preAllocate)
    addInfoComponentPool(preAllocate)
    addLevelMapComponentPool(preAllocate)
}

/**
 * Add here all data pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksDataPools(preAllocate: Int = 0) {
    addPointDataPool(preAllocate)
}