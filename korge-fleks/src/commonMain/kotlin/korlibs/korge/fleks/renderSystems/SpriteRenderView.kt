package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.datastructure.iterators.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*


/**
 * Creates a new [SpriteRenderView], allowing to configure with [callback], and attaches the newly created view to the
 * receiver "this".
 */
inline fun Container.spriteRenderView(viewPortSize: SizeInt, world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker SpriteRenderView.() -> Unit = {}) =
    SpriteRenderView(viewPortSize, world, layerTag).addTo(this, callback)

class SpriteRenderView(
    private val viewPortSize: SizeInt,
    world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[SpriteComponent].layerIndex.compareTo(entB[SpriteComponent].layerIndex) }
) : View() {
    private val family: Family

    override fun renderInternal(ctx: RenderContext) {
        // Custom Render Code here
        ctx.useBatcher { batch ->
            // Sort sprite entities by their layerIndex
            family.sort(comparator)

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val (x, y) = entity[PositionComponent]
                val (name, anchorX, anchorY, _, animation, frameIndex) = entity[SpriteComponent]
                val (rgba) = entity[RgbaComponent]
                val imageFrame = AssetStore.getImageFrame(name, animation, frameIndex)

                // Iterate over all layers of each sprite for the frame number
                imageFrame.layerData.fastForEachReverse { layer ->

                    batch.drawQuad(
                        tex = ctx.getTex(layer.slice),
                        x = x + layer.targetX - anchorX,
                        y = y + layer.targetY - anchorY,
                        filtering = false,
                        colorMul = rgba,
                        // TODO: Add possiblility to use a custom shader - add ShaderComponent or similar
                        program = null
                    )
                }
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
