package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.assets.data.ParallaxConfig.Mode.*
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent
import korlibs.korge.fleks.components.Position.Companion.PositionComponent


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

        // Update local positions for each layer which is configured to be moving (speedFactor not null or zero)
        parallaxComponent.bgLayerEntities.forEach { (layerName, layerEntity) -> update(layerName, layerEntity, motionComponent) }
        parallaxComponent.fgLayerEntities.forEach { (layerName, layerEntity) -> update(layerName, layerEntity, motionComponent) }

        // Update parallax plane lines
        parallaxComponent.parallaxPlane.linePositions.forEachIndexed { index, linePosition ->
            val parallaxConfig = assetStore.getParallaxConfig(parallaxComponent.name)
            val planeConfig = assetStore.getParallaxPlane(parallaxComponent.parallaxPlane.name)
            val speedFactor = planeConfig.lineTextures[index].speedFactor
            // Update linePosition of parallax plane line
            val lineLength = when (parallaxConfig.mode) {
                HORIZONTAL_PLANE -> {
                    parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + linePosition
                    planeConfig.lineTextures[index].bmpSlice.width
                }
                VERTICAL_PLANE -> {
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
                HORIZONTAL_PLANE -> {
                    parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.topAttachedLayerTextures[index].bmpSlice.width
                }
                VERTICAL_PLANE -> {
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
                HORIZONTAL_PLANE -> {
                    parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.bottomAttachedLayerTextures[index].bmpSlice.width
                }
                VERTICAL_PLANE -> {
                    parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] =
                        (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                    planeConfig.bottomAttachedLayerTextures[index].bmpSlice.height
                }
                else -> 0
            }
            // Check layer positions and wrap around the texture size
            parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index], max = layerLength)
        }

/*
        val offset = parallaxConfig.offset
        parallaxComponent.attachedLayersRearPositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersRear!!.defaultAnimation.firstFrame.layerData[index]
            val attachTextureOffset =
                if (plane.attachedLayersRear!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.attachedLayersRearPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.attachedLayersRearPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxComponent.linePositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetY - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                parallaxComponent.linePositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetX - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                parallaxComponent.linePositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxComponent.attachedLayersFrontPositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersFront!!.defaultAnimation.firstFrame.layerData[index]
            val attachTextureOffset = if (plane.attachedLayersFront!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.attachedLayersFrontPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.attachedLayersFrontPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxComponent.fgLayerEntities.fastForEachWithIndex { index, layerEntity ->
            val speedFactor: Float = parallaxDataContainer.config.foregroundLayers!![index].speedFactor ?: 0f

            val positionComponent = layerEntity[PositionComponent]
            // Check if layer has MotionComponent for self-movement
            val layerVelocityX = if (layerEntity has MotionComponent) layerEntity[MotionComponent].velocityX else 0f
            val layerVelocityY = if (layerEntity has MotionComponent) layerEntity[MotionComponent].velocityY else 0f

            // Calculate new position based on speed factor and layer velocity
            positionComponent.x = (speedFactor * motionComponent.velocityX + layerVelocityX) * worldToPixelRatio * deltaTime + positionComponent.x  // f(x) = v * t + x
            positionComponent.y = (speedFactor * motionComponent.velocityY + layerVelocityY) * worldToPixelRatio * deltaTime + positionComponent.y
        }
*/
    }

    private fun update(layerName: String, layerEntity: Entity, motionComponent: Motion) {
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

    private fun wrap(value: Float, max: Int, min: Int = 0): Float =
        if (value >= max) value - max else if (value < min) value + max else value
}
