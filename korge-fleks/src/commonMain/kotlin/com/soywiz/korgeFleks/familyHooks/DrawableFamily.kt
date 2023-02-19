package com.soywiz.korgeFleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import com.soywiz.korge.input.mouse
import com.soywiz.korge.tiled.TiledMapView
import com.soywiz.korge.view.*
import com.soywiz.korgeFleks.assets.AssetStore
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.components.Sprite
import com.soywiz.korgeFleks.components.Text
import com.soywiz.korgeFleks.systems.KorgeViewSystem
import com.soywiz.korgeFleks.korlibsAdaptation.ImageAnimView
import com.soywiz.korgeFleks.utils.KorgeViewCache
import com.soywiz.korgeFleks.korlibsAdaptation.ParallaxDataView
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.text.DefaultStringTextRenderer
import com.soywiz.korim.text.HorizontalAlign
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korim.text.VerticalAlign


/**
 * Whenever a View based object is created (combination of "all", "any" and "none") then the View objects can be
 * created and added to the [KorgeViewSystem]. This is done in [onEntityAdded] family hook here rather than in
 * component hook because we are sure that all needed components are set up before in the [World].
 */
object DrawableFamily {
    val define: Family = World.family { all(Drawable, PositionShape).any(Drawable, Appearance).none(ParallaxIntro) }

    val onEntityAdded: FamilyHook = { entity ->
        val assets = inject<AssetStore>()
        val layers = inject<HashMap<String, Container>>()
        val korgeViewCache = inject<KorgeViewCache>("normalViewCache")

        val drawable = entity[Drawable]
        val positionShape = entity[PositionShape]
        val width: Double
        val height: Double

        val view: View = when {
            entity has Sprite -> {
                val sprite = entity[Sprite]
                val view = ImageAnimView(assets.getImage(sprite.assetName), sprite.animationName, smoothing = false)

                // when animation finished playing trigger destruction of entity
                if (sprite.destroyOnPlayingFinished) view.onPlayFinished = {
                    entity.configure { entity ->
                        entity.getOrAdd(LifeCycle) { LifeCycle() }.also { it.healthCounter = 0 }
                    }
                }
                if (sprite.isPlaying) view.play(reverse = !sprite.forwardDirection, once = !sprite.loop)

                width =
                    view.data?.width?.toDouble() ?: error("KorgeViewHook: Cannot get width of sprite ImageAnimView!")
                height =
                    view.data?.height?.toDouble() ?: error("KorgeViewHook: Cannot get height of sprite ImageAnimView!")

                view
            }
            entity has TiledMap -> {
                // TODO get width and height of overall tiled map
                width = 0.0
                height = 0.0

                TiledMapView(assets.getTiledMap(entity[TiledMap].assetName), smoothing = false, showShapes = false)
            }
            entity has Parallax -> {
                // TODO set this depending of vertical or horizontal parallax effect
                width = 0.0  // TODO AppConfig.WIDTH.toDouble()
                height = 0.0  // TODO get height of parallax view

                val parallax = entity[Parallax]
                ParallaxDataView(
                    assets.getBackground(parallax.assetName),
                    disableScrollingX = parallax.disableScrollingX,
                    disableScrollingY = parallax.disableScrollingY
                )
            }
            entity has Text -> {
                val view = com.soywiz.korge.view.Text(
                    text = entity[Text].text,
                    textSize = com.soywiz.korge.view.Text.DEFAULT_TEXT_SIZE,
                    color = Colors.WHITE,
                    font = assets.getFont(entity[Text].fontName),
                    alignment = TextAlignment.CENTER,
                    renderer = DefaultStringTextRenderer,
                    autoScaling = false
                ).apply {
                    smoothing = false
                    verticalAlign = VerticalAlign.MIDDLE
                    horizontalAlign = HorizontalAlign.CENTER
                }
                width = view.width
                height = view.height
                view
            }
            else -> error("DrawableFamily.onEntityAdded: No Parallax, ParallaxLayer, TiledMap, Sprite or Text component found!")
        }

        entity.getOrNull(Appearance)?.also {
            view.visible = it.visible
            view.alpha = it.alpha
            it.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }
        }

        when (val layer = layers[drawable.drawOnLayer]) {
            null -> error("DrawableFamily.onEntityAdded: Cannot add view for entity '${entity.id}' to layer '${drawable.drawOnLayer}'!")
            else -> {
                layer.addChild(view)
                korgeViewCache.addOrUpdate(entity, view)
            }
        }

        entity.getOrNull(Layout)?.let {
            if (it.centerX) view.centerXBetween(0.0, view.root.width)  // <-- workaround for buggy centerXOnStage() of Text view
            if (it.centerY) view.centerYOnStage()
            positionShape.x = view.x + it.offsetX  // view is needed otherwise the Sprite System will not take possible center values from above
            positionShape.y = view.y + it.offsetY
        }

        entity.getOrNull(TouchInput)?.let {
            view.mouse {
                onDown {
                    println("onDown")
                }
                onUp {
                    println("onUp: Save world entities")
                }
                onUpOutside {
                    println("onUpOutside")
                }
            }
        }

        positionShape.width = width
        positionShape.height = height
    }

    val onEntityRemoved: FamilyHook = { entity ->
        (inject<KorgeViewCache>("normalViewCache").getOrNull(entity)
            ?: error("DrawableFamily.onEntityRemoved: Cannot remove view from entity '${entity.id}' with layer name '${entity[SpecificLayer].spriteLayer}'!"))
            .removeFromParent()
    }
}
