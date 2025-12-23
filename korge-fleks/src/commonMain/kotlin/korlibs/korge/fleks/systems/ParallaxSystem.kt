package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.ParallaxLayer
import korlibs.korge.fleks.assets.data.AssetConfig.ParallaxLayersInfo.Mode.*
import korlibs.korge.fleks.assets.data.ParallaxConfig
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Parallax
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent



fun Parallax.getBgLayerEntity(name: String): Entity {
    return if (bgLayerEntities.containsKey(name)) bgLayerEntities[name]!! else {
        println("ERROR: Parallax background layer '$name' not found in ParallaxComponent for '${this.name}'!")
        Entity.NONE
    }
}

fun Parallax.getFgLayerEntity(name: String): Entity {
    return if (fgLayerEntities.containsKey(name)) fgLayerEntities[name]!! else {
        println("ERROR: Parallax foreground layer '$name' not found in ParallaxComponent for '${this.name}'!")
        Entity.NONE
    }
}


class ParallaxSystem(
    private val worldToPixelRatio: Float
) : IteratingSystem(
    family = family { all(ParallaxComponent, MotionComponent) },
    interval = EachFrame
) {
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun onTickEntity(entity: Entity) {
        val parallaxComponent = entity[ParallaxComponent]
        val motionComponent = entity[MotionComponent]
        val parallaxConfig = assetStore.getParallaxConfigV2(parallaxComponent.name)

        // Update local positions for each layer which is configured to be moving (speedFactor not null or zero)
        parallaxConfig.backgroundLayers.forEach { layer -> update(layer, parallaxComponent.getBgLayerEntity(layer.name), motionComponent) }
        parallaxConfig.foregroundLayers.forEach { layer -> update(layer, parallaxComponent.getFgLayerEntity(layer.name), motionComponent) }

        // OLD -- remove later when V2 is fully adopted
        parallaxComponent.bgLayerEntities.forEach { (layerName, layerEntity) -> updateV1(layerName, layerEntity, motionComponent) }
        parallaxComponent.fgLayerEntities.forEach { (layerName, layerEntity) -> updateV1(layerName, layerEntity, motionComponent) }

        // Update parallax plane lines
        parallaxConfig.parallaxPlane?.let { parallaxPlane ->
            parallaxComponent.parallaxPlane.linePositions.forEachIndexed { index, linePosition ->
                val speedFactor = parallaxPlane.lineTextures[index].speedFactor
                // Update linePosition of parallax plane line
                val lineLength = when (parallaxConfig.mode) {
                    HORIZONTAL_PLANE -> {
                        parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + linePosition
                        parallaxPlane.lineTextures[index].bmpSlice.width
                    }
                    VERTICAL_PLANE -> {
                        parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + linePosition
                        parallaxPlane.lineTextures[index].bmpSlice.height
                    }
                    else -> 0
                }
                // Check line positions and wrap around the texture size
                parallaxComponent.parallaxPlane.linePositions[index] = wrap(parallaxComponent.parallaxPlane.linePositions[index], max = lineLength)
            }

            // Update top-attached layers of parallax plane
            parallaxComponent.parallaxPlane.topAttachedLayerPositions.forEachIndexed { index, layerPosition ->
                val speedFactor = parallaxPlane.topAttachedLayerTextures[index].speedFactor
                // Update position of parallax position
                val layerLength = when (parallaxConfig.mode) {
                    HORIZONTAL_PLANE -> {
                        parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                        parallaxPlane.topAttachedLayerTextures[index].bmpSlice.width
                    }
                    VERTICAL_PLANE -> {
                        parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                        parallaxPlane.topAttachedLayerTextures[index].bmpSlice.height
                    }
                    else -> 0
                }
                // Check layer positions and wrap around the texture size
                parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.topAttachedLayerPositions[index], max = layerLength)
            }

            // Update bottom-attached layers of parallax plane
            parallaxComponent.parallaxPlane.bottomAttachedLayerPositions.forEachIndexed { index, layerPosition ->
                val planeConfig = assetStore.getParallaxPlane(parallaxComponent.parallaxPlane.name)
                val speedFactor = planeConfig.bottomAttachedLayerTextures[index].speedFactor
                // Update position of parallax position
                val layerLength = when (parallaxConfig.mode) {
                    HORIZONTAL_PLANE -> {
                        parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                        planeConfig.bottomAttachedLayerTextures[index].bmpSlice.width
                    }
                    VERTICAL_PLANE -> {
                        parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                        planeConfig.bottomAttachedLayerTextures[index].bmpSlice.height
                    }
                    else -> 0
                }
                // Check layer positions and wrap around the texture size
                parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index], max = layerLength)
            }
        }

        // OLD
        parallaxComponent.parallaxPlane.linePositions.forEachIndexed { index, linePosition ->
            val parallaxConfig = assetStore.getParallaxConfig(parallaxComponent.name)
            val planeConfig = assetStore.getParallaxPlane(parallaxComponent.parallaxPlane.name)
            val speedFactor = planeConfig.lineTextures[index].speedFactor
            // Update linePosition of parallax plane line
            val lineLength = when (parallaxConfig.mode) {
                ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                    parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + linePosition
                    planeConfig.lineTextures[index].bmpSlice.width
                }
                ParallaxConfig.Mode.VERTICAL_PLANE -> {
                    parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + linePosition
                    planeConfig.lineTextures[index].bmpSlice.height
                }
                else -> 0
            }
            // Check line positions and wrap around the texture size
            parallaxComponent.parallaxPlane.linePositions[index] = wrap(parallaxComponent.parallaxPlane.linePositions[index], max = lineLength)
        }

        // Update top-attached layers of parallax plane
        parallaxComponent.parallaxPlane.topAttachedLayerPositions.forEachIndexed { index, layerPosition ->
            val parallaxConfig = assetStore.getParallaxConfig(parallaxComponent.name)
            val planeConfig = assetStore.getParallaxPlane(parallaxComponent.parallaxPlane.name)
            val speedFactor = planeConfig.topAttachedLayerTextures[index].speedFactor
            // Update position of parallax position
            val layerLength = when (parallaxConfig.mode) {
                ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                    parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.topAttachedLayerTextures[index].bmpSlice.width
                }
                ParallaxConfig.Mode.VERTICAL_PLANE -> {
                    parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.topAttachedLayerTextures[index].bmpSlice.height
                }
                else -> 0
            }
            // Check layer positions and wrap around the texture size
            parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.topAttachedLayerPositions[index], max = layerLength)
        }

        // Update bottom-attached layers of parallax plane
        parallaxComponent.parallaxPlane.bottomAttachedLayerPositions.forEachIndexed { index, layerPosition ->
            val parallaxConfig = assetStore.getParallaxConfig(parallaxComponent.name)
            val planeConfig = assetStore.getParallaxPlane(parallaxComponent.parallaxPlane.name)
            val speedFactor = planeConfig.bottomAttachedLayerTextures[index].speedFactor
            // Update position of parallax position
            val layerLength = when (parallaxConfig.mode) {
                ParallaxConfig.Mode.HORIZONTAL_PLANE -> {
                    parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.bottomAttachedLayerTextures[index].bmpSlice.width
                }
                ParallaxConfig.Mode.VERTICAL_PLANE -> {
                    parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.bottomAttachedLayerTextures[index].bmpSlice.height
                }
                else -> 0
            }
            // Check layer positions and wrap around the texture size
            parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index], max = layerLength)
        }
    }

    private fun updateV1(layerName: String, layerEntity: Entity, motionComponent: Motion) {
        val parallaxTexture = assetStore.getParallaxTexture(layerName)
        val speedFactor = parallaxTexture.speedFactor
        if (speedFactor != null) {
            val positionComponent = layerEntity[PositionComponent]
            // Check if layer has MotionComponent for self-movement - if not, use selfSpeed from ParallaxTexture config
            // With MotionComponent we can control self-movement with the TweenEngineSystem
            val layerVelocityX = if (layerEntity has MotionComponent) layerEntity[MotionComponent].velocityX else parallaxTexture.selfSpeedX
            val layerVelocityY = if (layerEntity has MotionComponent) layerEntity[MotionComponent].velocityY else parallaxTexture.selfSpeedY

            // Calculate new position based on speed factor and layer velocity
            positionComponent.x = (speedFactor * motionComponent.velocityX + layerVelocityX) * worldToPixelRatio * deltaTime + positionComponent.x  // f(x) = v * t + x
            positionComponent.y = (speedFactor * motionComponent.velocityY + layerVelocityY) * worldToPixelRatio * deltaTime + positionComponent.y

            // Wrap around movement of textures
            positionComponent.x = wrap(positionComponent.x, max = parallaxTexture.layerBmpSlice.width)
            positionComponent.y = wrap(positionComponent.y, max = parallaxTexture.layerBmpSlice.height)
        }
    }

    private fun update(parallaxLayer: ParallaxLayer, layerEntity: Entity, motionComponent: Motion) {
        val speedFactor = parallaxLayer.speedFactor
        if (speedFactor != null) {
            if (layerEntity hasNo PositionComponent) {
                println("ERROR: Parallax layer entity '${layerEntity.id}' (name: '${parallaxLayer.name}') has no PositionComponent!")
                return
            }
            val positionComponent = layerEntity[PositionComponent]
            // Check if layer has MotionComponent for self-movement - if not, use selfSpeed from ParallaxTexture config
            // With MotionComponent we can control self-movement with the TweenEngineSystem
            val layerVelocityX = if (layerEntity has MotionComponent) layerEntity[MotionComponent].velocityX else parallaxLayer.selfSpeedX
            val layerVelocityY = if (layerEntity has MotionComponent) layerEntity[MotionComponent].velocityY else parallaxLayer.selfSpeedY

            // Calculate new position based on speed factor and layer velocity
            positionComponent.x = (speedFactor * motionComponent.velocityX + layerVelocityX) * worldToPixelRatio * deltaTime + positionComponent.x  // f(x) = v * t + x
            positionComponent.y = (speedFactor * motionComponent.velocityY + layerVelocityY) * worldToPixelRatio * deltaTime + positionComponent.y

            // Wrap around movement of textures
            positionComponent.x = wrap(positionComponent.x, max = parallaxLayer.bmpSlice.width)
            positionComponent.y = wrap(positionComponent.y, max = parallaxLayer.bmpSlice.height)
        }
    }

    private fun wrap(value: Float, max: Int, min: Int = 0): Float =
        if (value >= max) value - max else if (value < min) value + max else value
}
