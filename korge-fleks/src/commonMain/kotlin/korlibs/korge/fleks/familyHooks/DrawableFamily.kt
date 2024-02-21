package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.DefaultStringTextRenderer
import korlibs.image.text.HorizontalAlign
import korlibs.image.text.TextAlignment
import korlibs.image.text.VerticalAlign
import korlibs.korge.assetmanager.AssetStore
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.Sprite
import korlibs.korge.fleks.components.Text
import korlibs.korge.fleks.entity.config.Invokable
import korlibs.korge.fleks.systems.KorgeViewSystem
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.input.mouse
import korlibs.korge.ldtk.view.*
import korlibs.korge.parallax.ImageDataViewEx
import korlibs.korge.parallax.ParallaxConfig
import korlibs.korge.parallax.ParallaxDataView
import korlibs.korge.view.*
import korlibs.korge.view.align.centerXOnStage
import korlibs.korge.view.align.centerYOnStage


/**
 * Whenever a View based object is created (combination of "all", "any" and "none" in the family lambda) then the View objects can be
 * created and added to the [KorgeViewSystem]. This is done in [onDrawableFamilyAdded] family hook here rather than in
 * component hook because we are sure that all needed components are set up before in the [World].
 */
fun drawableFamily(): Family = World.family { all(Drawable, PositionShapeComponent).any(Drawable, Appearance, InputTouchButton) }

val onDrawableFamilyAdded: FamilyHook = { entity ->
    val world = this
    val layers = inject<HashMap<String, Container>>("Layers")

    val drawable = entity[Drawable]
    val positionShapeComponent = entity[PositionShapeComponent]
    val width: Double
    val height: Double

    val view: View = when {
        entity has Sprite -> {
            val sprite = entity[Sprite]
            val view = ImageDataViewEx(AssetStore.getImage(sprite.assetName), sprite.animationName, smoothing = false)

            // when animation finished playing trigger destruction of entity
            if (sprite.destroyOnPlayingFinished) view.onPlayFinished = {
                entity.configure { entity ->
                    entity.getOrAdd(LifeCycle) { LifeCycle() }.also { it.healthCounter = 0 }
                }
            }
            if (sprite.isPlaying) view.play(reverse = !sprite.forwardDirection, once = !sprite.loop)

            width =
                view.data?.width?.toDouble() ?: error("onDrawableFamilyAdded: Cannot get width of sprite ImageAnimView!")
            height =
                view.data?.height?.toDouble() ?: error("onDrawableFamilyAdded: Cannot get height of sprite ImageAnimView!")

            view
        }

// TODO
//        entity has TiledMap -> {
//            // TODO get width and height of overall tiled map
//            width = 0.0f
//            height = 0.0f
//
//            TiledMapView(assets.getTiledMap(entity[TiledMap].assetName), smoothing = false, showShapes = false)
//        }

        entity has LdtkLevelMap -> {
            val ldtkLevelMapComponent = entity[LdtkLevelMap]
            val ldtkWorld = AssetStore.getLdtkWorld(ldtkLevelMapComponent.worldName)
            val ldtkLevel = AssetStore.getLdtkLevel(ldtkWorld, ldtkLevelMapComponent.levelName)
            val view = LDTKLevelView(level = LDTKLevel(
                world = ldtkWorld,
                level = ldtkLevel
            ))
            width = ldtkLevel.pxWid.toDouble()
            height = ldtkLevel.pxHei.toDouble()
            view
        }

        entity has Parallax -> {
            val parallax = entity[Parallax]
            val parallaxConfig = AssetStore.getBackground(parallax.config.name)
            val view = ParallaxDataView(parallaxConfig)

            when (parallaxConfig.config.mode) {
                ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                    width = parallaxConfig.config.parallaxPlane?.size?.width?.toDouble() ?: 0.0
                    height = view.parallaxLayerSize.toDouble()
                }
                ParallaxConfig.Mode.VERTICAL_PLANE -> {
                    width = view.parallaxLayerSize.toDouble()
                    height = parallaxConfig.config.parallaxPlane?.size?.height?.toDouble() ?: 0.0
                }
                else -> {
                    width = parallaxConfig.config.parallaxPlane?.size?.width?.toDouble() ?: 0.0
                    height = parallaxConfig.config.parallaxPlane?.size?.height?.toDouble() ?: 0.0
                }
            }

            view
        }

        entity has Text -> {
            val view = korlibs.korge.view.Text(
                text = entity[Text].text,
                textSize = korlibs.korge.view.Text.DEFAULT_TEXT_SIZE,
                color = Colors.WHITE,
                font = AssetStore.getFont(entity[Text].fontName),
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

        else -> error("onDrawableFamilyAdded: No Parallax, ParallaxLayer, TiledMap, Sprite or Text component found!")
    }

    entity.getOrNull(Appearance)?.also {
        view.visible = it.visible
        view.alpha = it.alpha.toDouble()
        it.tint?.also { tint -> view.colorMul = RGBA(tint.r, tint.g, tint.b, 0xff) }
    }

    when (val layer = layers[drawable.drawOnLayer]) {
        null -> error("onDrawableFamilyAdded: Cannot add view for entity '${entity.id}' to layer '${drawable.drawOnLayer}'!")
        else -> {
            layer.addChild(view)
            KorgeViewCache.addOrUpdate(entity, view)
        }
    }

    // Update position of view with initial position
    view.x = positionShapeComponent.x
    view.y = positionShapeComponent.y.toDouble()

    if (entity has Layout) {
        val layout = entity[Layout]
        if (layout.centerX) view.centerXOnStage()
        if (layout.centerY) view.centerYOnStage()
        positionShapeComponent.x = view.x + layout.offsetX  // view is needed otherwise the Sprite System will not take possible center values from above
        positionShapeComponent.y = view.y + layout.offsetY
    }

    // Set properties in TouchInput when touch input was recognized
    // TouchInputSystem checks for those properties and executes specific Invokable function
    entity.getOrNull(InputTouchButton)?.let { touchInput ->
        view.mouse {
            onDown {
                if (touchInput.triggerImmediately) Invokable.invoke(touchInput.function, world, entity, touchInput.config)
                touchInput.pressed = true
            }
            onUp {
                if (touchInput.pressed) {
                    touchInput.pressed = false
                    Invokable.invoke(touchInput.function, world, entity, touchInput.config)
                }
            }
            onUpOutside {
                if (touchInput.pressed) {
                    touchInput.pressed = false
                    if (touchInput.triggerImmediately) Invokable.invoke(touchInput.function, world, entity, touchInput.config)
                }
            }
        }
    }

    positionShapeComponent.width = width
    positionShapeComponent.height = height
}

val onDrawableFamilyRemoved: FamilyHook = { entity ->
    (KorgeViewCache.getOrNull(entity) ?: error("onDrawableFamilyRemoved: Cannot remove view of entity '${entity.id}' from layer '${entity[Drawable].drawOnLayer}'!")).removeFromParent()
    KorgeViewCache.remove(entity)
}
