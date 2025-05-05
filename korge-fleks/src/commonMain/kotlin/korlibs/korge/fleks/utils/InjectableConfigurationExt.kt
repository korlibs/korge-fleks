package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Collision.Companion.addCollisionComponentPool
import korlibs.korge.fleks.components.CoolDown.Companion.addCoolDownComponentPool
import korlibs.korge.fleks.components.EntityRef.Companion.addEntityRefComponentPool
import korlibs.korge.fleks.components.EntityRefs.Companion.addEntityRefsComponentPool
import korlibs.korge.fleks.components.EntityRefsByName.Companion.addEntityRefsByNameComponentPool
import korlibs.korge.fleks.components.Event.Companion.addEventComponentPool
import korlibs.korge.fleks.components.Info.Companion.addInfoComponentPool
import korlibs.korge.fleks.components.Layer.Companion.addLayerComponentPool
import korlibs.korge.fleks.components.LayeredSprite.Companion.addLayeredSpriteComponentPool
import korlibs.korge.fleks.components.LevelMap.Companion.addLevelMapComponentPool
import korlibs.korge.fleks.components.Position.Companion.addPositionComponentPool
import korlibs.korge.fleks.components.data.EntityVar.Companion.addEntityVarDataPool
import korlibs.korge.fleks.components.data.Point.Companion.addPointDataPool
import korlibs.korge.fleks.components.data.Rgb.Companion.addRgbDataPool
import korlibs.korge.fleks.components.data.TextureRef.Companion.addTextureRefDataPool

/**
 * Add here all component pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksComponentPools(preAllocate: Int = 0) {
    addCollisionComponentPool(preAllocate)
    addCoolDownComponentPool(preAllocate)
    addEntityRefComponentPool(preAllocate)
    addEntityRefsComponentPool(preAllocate)
    addEntityRefsByNameComponentPool(preAllocate)
    addEventComponentPool(preAllocate)
    addInfoComponentPool(preAllocate)
    addLayerComponentPool(preAllocate)
    addLayeredSpriteComponentPool(preAllocate)

    addPositionComponentPool(preAllocate)

    addLevelMapComponentPool(preAllocate)

}

/**
 * Add here all data pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksDataPools(preAllocate: Int = 0) {
    addEntityVarDataPool(preAllocate)
    addPointDataPool(preAllocate)
    addRgbDataPool(preAllocate)
    addTextureRefDataPool(preAllocate)
}