package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Layer.Companion.LayerComponent
import korlibs.korge.fleks.components.WorldMap.Companion.WorldMapComponent
import korlibs.korge.fleks.components.Position
import korlibs.korge.fleks.systems.SystemRuntimeConfigs
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.AppConfig
import korlibs.korge.render.*


/**
 * RenderSystem to render level maps. It uses the [WorldMapComponent] to determine which level maps should be rendered
 * and in which order. The [LayerComponent] is used to determine the rendering order of the level maps.
 * The [RenderLayerTag] is used to determine the layer on which the level maps should be rendered.
 * The [AssetStore] is used to retrieve the level maps and their data.
 * The [EntityComparator] is used to sort the level maps by their layer index.
 *
 * @param world the world containing the entities to render
 * @param layerName the name of the layer to render (e.g. "background", "main", "foreground")
 */
class LevelMapRenderSystem(
    private val world: World,
    private val layerName: String
) : RenderSystem {
    private val systemRuntimeConfigs = world.inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    private val assetStore = world.inject<AssetStore>("AssetStore")

    override fun render(ctx: RenderContext) {
        // Get main camera position or exit if it does not exist
        val cameraPosition: Position = systemRuntimeConfigs.getCameraPositionComponent(world) ?: return

        val tileSize = assetStore.worldMapData.tileSize

        // Calculate viewport position in world coordinates from Camera position (x,y) + offset
        val viewPortPosX: Float = cameraPosition.x + cameraPosition.offsetX - AppConfig.VIEW_PORT_WIDTH_HALF
        val viewPortPosY: Float = cameraPosition.y + cameraPosition.offsetY - AppConfig.VIEW_PORT_HEIGHT_HALF
        // Start and end indexes of viewport area (in tile coordinates)
        val xStart: Int = viewPortPosX.toInt() / tileSize - 1  // x in positive direction;  -1 = start one tile before
        val xTiles = (AppConfig.VIEW_PORT_WIDTH / tileSize) + 3
        val yStart: Int = viewPortPosY.toInt() / tileSize - 1  // y in negative direction;  -1 = start one tile before
        val yTiles = (AppConfig.VIEW_PORT_HEIGHT / tileSize) + 3

        ctx.useBatcher { batch ->
            // Iterate over all tiles in the visible area of the view port
            assetStore.worldMapData.forEachTile(layerName, xStart, yStart, xTiles, yTiles) { slice, px, py ->
                batch.drawQuad(
                    tex = ctx.getTex(slice),
                    x = px - viewPortPosX,
                    y = py - viewPortPosY,
                    filtering = false,
                    // colorMul = rgba,
                    program = null // Possibility to use a custom shader - add ShaderComponent or similar
                )
            }
        }
    }
}
