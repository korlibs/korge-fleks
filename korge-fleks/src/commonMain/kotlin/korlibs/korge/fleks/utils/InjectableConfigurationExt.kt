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
import korlibs.korge.fleks.components.LifeCycle.Companion.addLifeCycleComponentPool
import korlibs.korge.fleks.components.Motion.Companion.addMotionComponentPool
import korlibs.korge.fleks.components.NinePatch.Companion.addNinePatchComponentPool
import korlibs.korge.fleks.components.OffsetByFrameIndex.Companion.addOffsetByFrameIndexComponentPool
import korlibs.korge.fleks.components.Parallax.Companion.addParallaxComponentPool
import korlibs.korge.fleks.components.Platformer.Companion.addPlatformerComponentPool
import korlibs.korge.fleks.components.Position.Companion.addPositionComponentPool
import korlibs.korge.fleks.components.Rgba.Companion.addRgbaComponentPool
import korlibs.korge.fleks.components.Rigidbody.Companion.addRigidbodyComponentPool
import korlibs.korge.fleks.components.Size.Companion.addSizeComponentPool
import korlibs.korge.fleks.components.SizeInt.Companion.addSizeIntComponentPool
import korlibs.korge.fleks.components.Sound.Companion.addSoundComponentPool
import korlibs.korge.fleks.components.Spawner.Companion.addSpawnerComponentPool
import korlibs.korge.fleks.components.Sprite.Companion.addSpriteComponentPool
import korlibs.korge.fleks.components.SpriteLayers.Companion.addSpriteLayersComponentPool
import korlibs.korge.fleks.components.SwitchLayerVisibility.Companion.addSwitchLayerVisibilityComponentPool
import korlibs.korge.fleks.components.TextField.Companion.addTextFieldComponentPool
import korlibs.korge.fleks.components.TouchInput.Companion.addTouchInputComponentPool
import korlibs.korge.fleks.components.TweenProperty.Companion.addTweenPropertyComponentPools
import korlibs.korge.fleks.components.data.Point.Companion.addPointDataPool
import korlibs.korge.fleks.components.data.Rgb.Companion.addRgbDataPool
import korlibs.korge.fleks.components.data.SpriteLayer.Companion.addSpriteLayerDataPool
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
    addLevelMapComponentPool(preAllocate)
    addLifeCycleComponentPool(preAllocate)
    addMotionComponentPool(preAllocate)
    addNinePatchComponentPool(preAllocate)
    addOffsetByFrameIndexComponentPool(preAllocate)
    addParallaxComponentPool(preAllocate)
    addPositionComponentPool(preAllocate)
    addRgbaComponentPool(preAllocate)
    addRigidbodyComponentPool(preAllocate)
    addSizeComponentPool(preAllocate)
    addSizeIntComponentPool(preAllocate)
    addSoundComponentPool(preAllocate)
    addSpawnerComponentPool(preAllocate)
    addSpriteComponentPool(preAllocate)
    addSpriteLayersComponentPool(preAllocate)
    addSwitchLayerVisibilityComponentPool(preAllocate)
    addTextFieldComponentPool(preAllocate)
    addTouchInputComponentPool(preAllocate)
    addTweenPropertyComponentPools(preAllocate)
    addPlatformerComponentPool(preAllocate)
}

/**
 * Add here all data pools which are used in the game.
 */
fun InjectableConfiguration.addKorgeFleksDataPools(preAllocate: Int = 0) {
    addPointDataPool(preAllocate)
    addRgbDataPool(preAllocate)
    addSpriteLayerDataPool(preAllocate)
    addTextureRefDataPool(preAllocate)
}