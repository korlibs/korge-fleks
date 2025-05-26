package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.collection.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Layer.Companion.LayerComponent
import korlibs.korge.fleks.components.LevelMap.Companion.LevelMapComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.render.*
import korlibs.korge.view.*
import korlibs.math.geom.*


inline fun Container.levelMapRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker LevelMapRenderSystem.() -> Unit = {}) =
    LevelMapRenderSystem(world, layerTag).addTo(this, callback)

/**
 * RenderSystem to render level maps. It uses the [LevelMapComponent] to determine which level maps should be rendered
 * and in which order. The [LayerComponent] is used to determine the rendering order of the level maps.
 * The [RenderLayerTag] is used to determine the layer on which the level maps should be rendered.
 * The [AssetStore] is used to retrieve the level maps and their data.
 * The [EntityComparator] is used to sort the level maps by their layer index.
 *
 * @param world the world containing the entities to render
 * @param layerTag the tag to determine the layer on which the level maps should be rendered
 * @param comparator the comparator to sort the level maps by their layer index
 */
class LevelMapRenderSystem(
    private val world: World,
    layerTag: RenderLayerTag,
    private val comparator: EntityComparator = compareEntity(world) { entA, entB -> entA[LayerComponent].index.compareTo(entB[LayerComponent].index) }
) : View() {
    private val family: Family = world.family { all(layerTag, LayerComponent, LevelMapComponent) }

    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun renderInternal(ctx: RenderContext) {
        val camera: Entity = world.getMainCamera()
        val cameraPosition = with(world) { camera[PositionComponent] }

        // Sort level maps by their layerIndex
        family.sort(comparator)

        // Iterate over all entities which should be rendered in this view
        family.forEach { entity ->
            val rgba = entity[RgbaComponent].rgba
            val levelMap = entity[LevelMapComponent]
            val worldData = assetStore.getWorldData(levelMap.levelName)
            val tileSize = worldData.tileSize

            // Calculate viewport position in world coordinates from Camera position (x,y) + offset
            val viewPortPosX: Float = cameraPosition.x + cameraPosition.offsetX - AppConfig.VIEW_PORT_WIDTH_HALF
            val viewPortPosY: Float = cameraPosition.y + cameraPosition.offsetY - AppConfig.VIEW_PORT_HEIGHT_HALF

            // Start and end indexes of viewport area (in tile coordinates)
            val xStart: Int = viewPortPosX.toInt() / tileSize - 1  // x in positive direction;  -1 = start one tile before
            val xTiles = (AppConfig.VIEW_PORT_WIDTH / tileSize) + 3

            val yStart: Int = viewPortPosY.toInt() / tileSize - 1  // y in negative direction;  -1 = start one tile before
            val yTiles = (AppConfig.VIEW_PORT_HEIGHT / tileSize) + 3

            levelMap.layerNames.forEach { layerName ->
                ctx.useBatcher { batch ->
                    worldData.forEachTile(layerName, xStart, yStart, xTiles, yTiles) { slice, px, py ->
                        batch.drawQuad(
                            tex = ctx.getTex(slice),
                            x = px - viewPortPosX,
                            y = py - viewPortPosY,
                            filtering = false,
                            colorMul = rgba,
                            program = null // Possibility to use a custom shader - add ShaderComponent or similar
                        )
                    }
                }
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
