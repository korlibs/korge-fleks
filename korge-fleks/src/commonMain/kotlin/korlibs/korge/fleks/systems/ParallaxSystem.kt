package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.datastructure.iterators.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Motion.Companion.MotionComponent
import korlibs.korge.fleks.components.Parallax.Companion.ParallaxComponent


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
        val parallaxDataContainer = assetStore.getBackground(parallaxComponent.name)
        val offset = parallaxDataContainer.config.offset

        // Update local positions for each layer which is configured to be moving (speedFactor not null or zero)
        parallaxComponent.backgroundLayers.fastForEachWithIndex { index, layer ->
            val speedFactor = parallaxDataContainer.config.backgroundLayers!![index].speedFactor
            if (speedFactor != null) {
                layer.position.x = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layer.position.x  // f(x) = v * t + x
                layer.position.y = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layer.position.y
            }
        }

        parallaxComponent.parallaxPlane.attachedLayersRearPositions.fastForEachWithIndex { index, position ->
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
                parallaxComponent.parallaxPlane.attachedLayersRearPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.parallaxPlane.attachedLayersRearPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxComponent.parallaxPlane.linePositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetY - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetX - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                parallaxComponent.parallaxPlane.linePositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxComponent.parallaxPlane.attachedLayersFrontPositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersFront!!.defaultAnimation.firstFrame.layerData[index]
            val attachTextureOffset = if (plane.attachedLayersFront!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.parallaxPlane.attachedLayersFrontPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - offset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxComponent.parallaxPlane.attachedLayersFrontPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxComponent.foregroundLayers.fastForEachWithIndex { index, layer ->
            val speedFactor: Float = parallaxDataContainer.config.foregroundLayers!![index].speedFactor ?: 0f
            val selfSpeedX = parallaxDataContainer.config.foregroundLayers[index].selfSpeedX
            val selfSpeedY = parallaxDataContainer.config.foregroundLayers[index].selfSpeedY
            layer.position.x = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + selfSpeedX * deltaTime + layer.position.x
            layer.position.y = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layer.position.y
        }
    }
}
