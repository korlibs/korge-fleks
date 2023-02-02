package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.familyHooks.SpecificLayerFamily

/**
 *
 *
 */
class DebugSystem(
//    private val korgeViewCache: KorgeViewCache = World.inject("normalViewCache"),
//    private val layers: HashMap<String, Container> = World.inject(),
//    private val assets: GameAssets = World.inject()
) : IteratingSystem(
    family { all(AssetReload, Drawable, PositionShape) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val assetReload = entity[AssetReload]
        if (assetReload.trigger) {
            SpecificLayerFamily.onEntityRemoved(world, entity)
            SpecificLayerFamily.onEntityAdded(world, entity)

            // TODO check DrawableLayerFamily !!!

            assetReload.trigger = false
        }
/*            entity.getOrNull(Parallax)?.let { parallax ->
                // Remove old view
                korgeViewCache.getOrNull(entity)?.removeFromParent()

                // Create new view object with updated assets
                val view = ParallaxDataView(assets.getBackground(parallax.assetName), disableScrollingX = parallax.disableScrollingX, disableScrollingY = parallax.disableScrollingY)

                if (layers[drawable.layerName] != null) {
                val layers = inject<HashMap<String, Container>>()
                layers[drawable.layerName]!!.addChild(view)
                korgeViewCache.addOrUpdate(entity, view)

            }
        }
*/
    }
}
