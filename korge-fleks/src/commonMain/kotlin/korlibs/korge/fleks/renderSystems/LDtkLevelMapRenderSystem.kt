package korlibs.korge.fleks.renderSystems

import com.github.quillraven.fleks.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.ldtk.view.*
import korlibs.korge.view.*
import korlibs.korge.view.align.*
import korlibs.math.geom.*


inline fun Container.ldtkLevelMapRenderSystem(world: World, layerTag: RenderLayerTag, callback: @ViewDslMarker LDtkLevelMapRenderSystem.() -> Unit = {}) =
    LDtkLevelMapRenderSystem(world, layerTag).addTo(this, callback)

/**
 * Here we do not render the actual level map yet.
 * Instead, we add the view object for the level map to the container.
 */
class LDtkLevelMapRenderSystem(
    world: World,
    layerTag: RenderLayerTag
) : Container() {
    private val family: Family
    private var levelMapEntity: Entity = Entity.NONE
    private var levelMapView: View? = null
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    init {
        name = layerTag.toString()
        family = world.family { all(layerTag, PositionComponent, LdtkLevelMapComponent, RgbaComponent) }

        addUpdater {
            // In this updater we add *ONE* level map view to this container
            // An entity which defines the level map and contains the specific "layerTag" will be used to
            // create the LevelMapView

            if (family.numEntities > 1) println("WARNING - LDtkLevelMapRenderView: More than one entity for '$layerTag' found!. Only first one will be used!")

            family.firstOrNull()?.let { entity ->
                with (family) {
                    // First do initialization steps if needed
                    if (levelMapEntity != entity) {
                        levelMapEntity = entity
                        // Possibly remove old level map view from container
                        levelMapView?.removeFromParent()

                        // Create new view for the level map and add it to the container
                        val ldtkLevelMapComponent = entity[LdtkLevelMapComponent]
                        val ldtkWorld = assetStore.getLdtkWorld(ldtkLevelMapComponent.worldName)
                        val ldtkLevel = assetStore.getLdtkLevel(ldtkWorld, ldtkLevelMapComponent.levelName)
                        levelMapView = LDTKLevelView(
                            level = LDTKLevel(
                                world = ldtkWorld,
                                level = ldtkLevel
                            )
                        )
                        addChild(levelMapView!!)
                        println("Add entity ${entity.id} to '$name'")

                        // Check if we need to center the view
                        if (entity has LayoutComponent) {
                            val layout = entity[LayoutComponent]
                            val positionComponent = entity[PositionComponent]
                            if (layout.centerX) levelMapView!!.centerXOnStage()
                            if (layout.centerY) levelMapView!!.centerYOnStage()
                            positionComponent.x = levelMapView!!.x.toFloat() + layout.offsetX  // view is needed otherwise the Sprite System will not take possible center values from above
                            positionComponent.y = levelMapView!!.y.toFloat() + layout.offsetY
                        }
                    }

                    // Second apply continuously position and color to the view
                    val (rgba) = entity[RgbaComponent]
                    val (x, y) = entity[PositionComponent]
                    levelMapView!!.tint = rgba
                    levelMapView!!.pos = Point(x, y)
                }
            }
        }
    }
}
