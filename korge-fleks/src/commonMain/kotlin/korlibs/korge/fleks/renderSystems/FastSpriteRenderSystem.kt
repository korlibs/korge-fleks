package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*


/**
 * Creates a new [FastSpriteRenderSystem], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * The [FastSpriteRenderSystem] is rendering one texture per sprite to make it faster. It just takes the first layer
 * of an Aseprite file and ignored additional layers. Also, it does not sort the entities before rendering them.
 * This should be used for explosion and dust effects where the order of drawn textures is not significant.
 */
inline fun Container.fastSpriteRenderSystem(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker FastSpriteRenderSystem.() -> Unit = {}) =
    FastSpriteRenderSystem(viewPortSize, world, layerTag).addTo(this, callback)

class FastSpriteRenderSystem(
    private val viewPortSize: SizeInt,
    world: World,
    layerTag: RenderLayerTag
) : View() {
    private val family: Family
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code here
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val (x, y) = entity[PositionComponent]
                val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                val (rgba) = entity[RgbaComponent]
                val imageFrame = assetStore.getImageFrame(name, animation, frameIndex)

                // Just take the first layer of an Aseprite image file
                val texture = imageFrame.first ?: return@forEach

                batch.drawQuad(
                    tex = ctx.getTex(texture.slice),
                    x = x + texture.targetX - anchorX,
                    y = y + texture.targetY - anchorY,
                    filtering = false,
                    colorMul = rgba,
                    program = null
                )
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle =
        Rectangle(0, 0, viewPortSize.width, viewPortSize.height)

    init {
        name = layerTag.toString()
        family = world.family { all(layerTag, PositionComponent, SpriteComponent, RgbaComponent) }
    }
}
