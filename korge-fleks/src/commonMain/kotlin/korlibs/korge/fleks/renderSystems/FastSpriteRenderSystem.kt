package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Layer.Companion.LayerComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Position.Companion.staticPositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.SpriteComponent
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


class FastSpriteRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag
) : RenderSystem {
    private val family = world.family { all(layerTag, PositionComponent, SpriteComponent, RgbaComponent).none(LayerComponent) }
    private val assetStore: AssetStore = world.inject(name = "AssetStore")
    private val position: Position = staticPositionComponent {}

    override fun render(ctx: RenderContext) {
        val camera: Entity = world.getMainCameraOrNull() ?: return

        // Custom Render Code here
        ctx.useBatcher { batch ->

            // Iterate over all entities which should be rendered in this view
            family.forEach { entity ->
                val entityPosition = entity[PositionComponent]

                // Take over entity position
                position.init(entityPosition)

                if (entity hasNo ScreenCoordinatesTag) {
                    // Transform world coordinates to screen coordinates
                    position.run { world.convertToScreenCoordinates(camera) }
                }

                val spriteComponent = entity[SpriteComponent]
                val rgba = entity[RgbaComponent].rgba
                val imageFrame = assetStore.getImageFrame(spriteComponent.name, spriteComponent.animation, spriteComponent.frameIndex)

                // Just take the first layer of an Aseprite image file
                val texture = imageFrame.first ?: return@forEach

                batch.drawQuad(
                    tex = ctx.getTex(texture.slice),
                    x = position.x + texture.targetX - spriteComponent.anchorX,
                    y = position.y + texture.targetY - spriteComponent.anchorY,
                    filtering = false,
                    colorMul = rgba,
                    program = null
                )
            }
        }
    }
}
