package korlibs.korge.fleks.familyHooks

import korlibs.korge.tiled.TiledMapView
import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.DefaultStringTextRenderer
import korlibs.image.text.HorizontalAlign
import korlibs.image.text.TextAlignment
import korlibs.image.text.VerticalAlign
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.Sprite
import korlibs.korge.fleks.components.Text
import korlibs.korge.fleks.systems.KorgeViewSystem
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.parallax.ImageDataViewEx
import korlibs.korge.parallax.ParallaxConfig
import korlibs.korge.parallax.ParallaxDataView
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXBetween
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.align.centerYOnStage


/**
 * Whenever a View based object is created (combination of "all", "any" and "none") then the View objects can be
 * created and added to the [KorgeViewSystem]. This is done in [onDrawableFamilyAdded] family hook here rather than in
 * component hook because we are sure that all needed components are set up before in the [World].
 */

fun drawableFamily(): Family = World.family { all(Drawable, PositionShape).any(Drawable, Appearance) }

val onDrawableFamilyAdded: FamilyHook = { entity ->
    val assets = inject<AssetStore>("AssetStore")
    val layers = inject<HashMap<String, Container>>("Layers")
    val korgeViewCache = inject<KorgeViewCache>("KorgeViewCache")

    val drawable = entity[Drawable]
    val positionShape = entity[PositionShape]
    val width: Float
    val height: Float

    val view: View = when {
        entity has Sprite -> {
            val sprite = entity[Sprite]
            val view = ImageDataViewEx(assets.getImage(sprite.assetName), sprite.animationName, smoothing = false)

            // when animation finished playing trigger destruction of entity
            if (sprite.destroyOnPlayingFinished) view.onPlayFinished = {
                entity.configure { entity ->
                    entity.getOrAdd(LifeCycle) { LifeCycle() }.also { it.healthCounter = 0 }
                }
            }
            if (sprite.isPlaying) view.play(reverse = !sprite.forwardDirection, once = !sprite.loop)

            width =
                view.data?.width?.toFloat() ?: error("KorgeViewHook: Cannot get width of sprite ImageAnimView!")
            height =
                view.data?.height?.toFloat() ?: error("KorgeViewHook: Cannot get height of sprite ImageAnimView!")

            view
        }

        entity has TiledMap -> {
            // TODO get width and height of overall tiled map
            width = 0.0f
            height = 0.0f

            TiledMapView(assets.getTiledMap(entity[TiledMap].assetName), smoothing = false, showShapes = false)
        }

        entity has Parallax -> {
            val parallax = entity[Parallax]
            val parallaxConfig = assets.getBackground(parallax.assetName)
            val view = ParallaxDataView(parallaxConfig)

            when (parallaxConfig.config.mode) {
                ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                    width = parallaxConfig.config.parallaxPlane?.size?.width?.toFloat() ?: 0.0f
                    height = view.parallaxLayerSize.toFloat()
                }
                ParallaxConfig.Mode.VERTICAL_PLANE -> {
                    width = view.parallaxLayerSize.toFloat()
                    height = parallaxConfig.config.parallaxPlane?.size?.height?.toFloat() ?: 0.0f
                }
                else -> {
                    width = parallaxConfig.config.parallaxPlane?.size?.width?.toFloat() ?: 0.0f
                    height = parallaxConfig.config.parallaxPlane?.size?.height?.toFloat() ?: 0.0f
                }
            }

            view
        }

        entity has Text -> {
            val view = korlibs.korge.view.Text(
                text = entity[Text].text,
                textSize = korlibs.korge.view.Text.DEFAULT_TEXT_SIZE,
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

    if (entity has Layout) {
        val layout = entity[Layout]
        if (layout.centerX) view.centerXOnStage()
        if (layout.centerY) view.centerYOnStage()
        positionShape.x = view.x + layout.offsetX  // view is needed otherwise the Sprite System will not take possible center values from above
        positionShape.y = view.y + layout.offsetY
    }

    positionShape.width = width
    positionShape.height = height
}

val onDrawableFamilyRemoved: FamilyHook = { entity ->
    val korgeViewCache = inject<KorgeViewCache>("KorgeViewCache")
    (korgeViewCache.getOrNull(entity)
        ?: error("DrawableFamily.onEntityRemoved: Cannot remove view of entity '${entity.id}' from layer '${entity[Drawable].drawOnLayer}'!"))
        .removeFromParent()
    korgeViewCache.remove(entity)
}
