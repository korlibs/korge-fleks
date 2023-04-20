package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.familyHooks.onSpecificLayerFamilyAdded
import korlibs.korge.fleks.familyHooks.onSpecificLayerFamilyRemoved
import korlibs.korge.fleks.components.AssetReload
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape

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
            onSpecificLayerFamilyRemoved(world, entity)
            onSpecificLayerFamilyAdded(world, entity)

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
