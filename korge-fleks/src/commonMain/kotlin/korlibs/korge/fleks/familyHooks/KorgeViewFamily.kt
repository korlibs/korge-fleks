package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import korlibs.image.text.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TextComponent
import korlibs.korge.fleks.systems.KorgeViewSystem
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.ldtk.view.*
import korlibs.korge.tiled.*
import korlibs.korge.view.*
import korlibs.math.geom.*


/**
 * Whenever a View based object is created (combination of "all", "any" and "none" in the family lambda) then the View objects can be
 * created and added to the [KorgeViewSystem]. This is done in [KorgeView.onAdded] family hook here rather than in
 * component hook because we are sure that all needed components are set up before in the [World].
 */
class KorgeView {
    val family: Family = World.family { all(ViewTag, PositionComponent, SizeComponent) }

    val onAdded: FamilyHook = { entity ->
        val positionComponent = entity[PositionComponent]
        val sizeComponent = entity[SizeComponent]
        val width: Double
        val height: Double

        val view: View = when {
            entity has TiledLevelMapComponent -> {
                // TODO get width and height of overall tiled map
                width = 0.0
                height = 0.0

                TiledMapView(AssetStore.getTiledMap(entity[TiledLevelMapComponent].assetName), smoothing = false, showShapes = false)
            }

            entity has LdtkLevelMapComponent -> {
                val ldtkLevelMapComponent = entity[LdtkLevelMapComponent]
                val ldtkWorld = AssetStore.getLdtkWorld(ldtkLevelMapComponent.worldName)
                val ldtkLevel = AssetStore.getLdtkLevel(ldtkWorld, ldtkLevelMapComponent.levelName)
                val view = LDTKLevelView(
                    level = LDTKLevel(
                        world = ldtkWorld,
                        level = ldtkLevel
                    )
                )
                width = ldtkLevel.pxWid.toDouble()
                height = ldtkLevel.pxHei.toDouble()
                view
            }

            entity has TextComponent -> {
                val component = entity[TextComponent]
                val richTextData = RichTextData(
                    text = component.text,
                    font = AssetStore.getFont(component.fontName)
                )
                val view = TextBlock(
                    text = richTextData,
                    align = TextAlignment.CENTER,
                    size = Size2D(richTextData.width + 1, richTextData.height + 1)
                ).apply {
                    smoothing = false
                }
                width = view.width
                height = view.height

                view
            }

            else -> error("onDrawableFamilyAdded: No Parallax, ParallaxLayer, TiledMap, Sprite or Text component found!")
        }
        KorgeViewCache.addOrUpdate(entity, view)

        // Update position of view with initial position
        view.x = positionComponent.x.toDouble()
        view.y = positionComponent.y.toDouble()

        sizeComponent.width = width.toFloat()
        sizeComponent.height = height.toFloat()
    }

    val onRemoved: FamilyHook = { entity ->
        KorgeViewCache[entity].removeFromParent()
        KorgeViewCache.remove(entity)
    }
}
