package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.Point.Companion.addPointDataPool
import korlibs.korge.fleks.components.data.Rgb.Companion.addRgbDataPool

/**
 * Add here all component pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksComponentPools(preAllocate: Int = 0) {
}

/**
 * Add here all data pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksDataPools(preAllocate: Int = 0) {
    addPointDataPool(preAllocate)
    addRgbDataPool(preAllocate)
}