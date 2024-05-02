package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*


/**
 * Creates a new [ParticleRenderView], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 *
 * The [ParticleRenderView] is rendering one texture per sprite to make it faster. It just takes the first layer
 * of an Aseprite file and ignored additional layers. Also, it does not sort the entities before rendering them.
 */
inline fun Container.particleRenderView(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker ParticleRenderView.() -> Unit = {}) =
    ParticleRenderView(viewPortSize, world, layerTag).addTo(this, callback)

class ParticleRenderView(
    private val viewPortSize: SizeInt,
    world: World,
    layerTag: RenderLayerTag
) : View() {
    private val family: Family

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code here
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val (x, y) = entity[PositionComponent]
                val (name, anchorX, anchorY, _, animation, frameIndex) = entity[SpriteComponent]
                val (rgba) = entity[RgbaComponent]
                val imageFrame = AssetStore.getImageFrame(name, animation, frameIndex)

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
        family = world.family { all(layerTag, PositionComponent, SpriteComponent, RgbaComponent)}
    }
}
