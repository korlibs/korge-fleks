package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.components.Drawable
import korlibs.korge.fleks.components.PositionShape
import korlibs.korge.fleks.components.Sprite
import korlibs.korge.fleks.familyHooks.*

/**
 *
 *
 */
class DebugSystem(
//    private val korgeViewCache: KorgeViewCache = World.inject("KorgeViewCache"),
//    private val layers: HashMap<String, Container> = World.inject(),
//    private val assets: GameAssets = World.inject()
) : IteratingSystem(
    family { all(Drawable, PositionShape) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
/*            entity.getOrNull(Parallax)?.let { parallax ->
                // Remove old view
                korgeViewCache.getOrNull(entity)?.removeFromParent()

                // Create new view object with updated assets
                val view = ParallaxDataView(assets.getBackground(parallax.assetName), disableScrollingX = parallax.disableScrollingX, disableScrollingY = parallax.disableScrollingY)

                if (layers[drawable.layerName] != null) {
                val layers = inject<HashMap<String, Container>>("Layers")
                layers[drawable.layerName]!!.addChild(view)
                korgeViewCache.addOrUpdate(entity, view)

            }
        }
*/
    }
}
