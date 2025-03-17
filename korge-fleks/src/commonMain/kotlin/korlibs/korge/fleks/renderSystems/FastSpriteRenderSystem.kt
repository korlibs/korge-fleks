package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
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
inline fun Container.fastSpriteRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker FastSpriteRenderSystem.() -> Unit = {}) =
    FastSpriteRenderSystem(world, layerTag).addTo(this, callback)

class FastSpriteRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag
) : View() {
    private val family = world.family { all(layerTag, PositionComponent, SpriteComponent, RgbaComponent) }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()

        // Custom Render Code here
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val entityPosition = entity[PositionComponent]

                val position: PositionComponent = if (entity has ScreenCoordinatesTag) {
                    // Take over entity coordinates
                    entityPosition
                } else {
                    // Transform world coordinates to screen coordinates
                    entityPosition.run {  world.convertToScreenCoordinates(camera) }
                }

                val (name, anchorX, anchorY, animation, frameIndex) = entity[SpriteComponent]
                val (rgba) = entity[RgbaComponent]
                val imageFrame = assetStore.getImageFrame(name, animation, frameIndex)

                // Just take the first layer of an Aseprite image file
                val texture = imageFrame.first ?: return@forEach

                batch.drawQuad(
                    tex = ctx.getTex(texture.slice),
                    x = position.x + texture.targetX - anchorX,
                    y = position.y + texture.targetY - anchorY,
                    filtering = false,
                    colorMul = rgba,
                    program = null
                )
            }
        }
    }

    // Set size of render view to display size
    override fun getLocalBoundsInternal(): Rectangle = with (world) {
        return Rectangle(0, 0, AppConfig.VIEW_PORT_WIDTH, AppConfig.VIEW_PORT_HEIGHT)
    }

    init {
        name = layerTag.toString()
    }
}
