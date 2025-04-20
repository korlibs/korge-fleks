package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Collision.Companion.addCollisionComponentPool
import korlibs.korge.fleks.components.LevelMap.Companion.addLevelMapComponentPool
import korlibs.korge.fleks.utils.poolableData.Point.Companion.addPointDataPool

fun InjectableConfiguration.addKorgeFleksComponentPools(preAllocate: Int = 0) {
    addCollisionComponentPool(preAllocate)
    addLevelMapComponentPool(preAllocate)
}

fun InjectableConfiguration.addKorgeFleksDataPools(preAllocate: Int = 0) {
    addPointDataPool(preAllocate)
}