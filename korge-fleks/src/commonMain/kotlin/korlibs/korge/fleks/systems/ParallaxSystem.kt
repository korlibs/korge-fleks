package korlibs.korge.fleks.systems

import korlibs.korge.fleks.components.ParallaxComponent
import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.datastructure.iterators.*
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.*


class ParallaxSystem(
    private val worldToPixelRatio: Float
) : IteratingSystem(
    family = family { all(ParallaxComponent, MotionComponent) },
    interval = EachFrame
) {
    private val assetStore: AssetStore = world.inject(name = "AssetStore")

    override fun onTickEntity(entity: Entity) {
        val (configName, backgroundLayers, parallaxPlane, foregroundLayers) = entity[ParallaxComponent]
        val (_, _, velocityX, velocityY) = entity[MotionComponent]
        val parallaxDataContainer = assetStore.getBackground(configName)

        // Update local positions for each layer which is configured to be moving (speedFactor not null or zero)
        backgroundLayers.fastForEachWithIndex { index, layer ->
            val speedFactor = parallaxDataContainer.config.backgroundLayers!![index].speedFactor
            if (speedFactor != null) {
                layer.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + layer.position.x  // f(x) = v * t + x
                layer.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + layer.position.y
            }
        }

        parallaxPlane.attachedLayersRearPositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersRear!!.defaultAnimation.firstFrame.layerData[index]
            val globalOffset = plane.offset
            val attachTextureOffset =
                if (plane.attachedLayersRear!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxPlane.attachedLayersRearPositions[index] = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxPlane.attachedLayersRearPositions[index] = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxPlane.linePositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val offset = plane.offset
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetY - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                parallaxPlane.linePositions[index] = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetX - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                parallaxPlane.linePositions[index] = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        parallaxPlane.attachedLayersFrontPositions.fastForEachWithIndex { index, position ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersFront!!.defaultAnimation.firstFrame.layerData[index]
            val globalOffset = plane.offset
            val attachTextureOffset = if (plane.attachedLayersFront!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == ParallaxConfig.Mode.HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxPlane.attachedLayersFrontPositions[index] = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + position
            } else if (parallaxMode == ParallaxConfig.Mode.VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax position
                parallaxPlane.attachedLayersFrontPositions[index] = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + position
            }
        }

        foregroundLayers.fastForEachWithIndex { index, layer ->
            val speedFactor: Float = parallaxDataContainer.config.foregroundLayers!![index].speedFactor ?: 0f
            val selfSpeedX = parallaxDataContainer.config.foregroundLayers[index].selfSpeedX
            val selfSpeedY = parallaxDataContainer.config.foregroundLayers[index].selfSpeedY
            layer.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + selfSpeedX * deltaTime + layer.position.x
            layer.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + layer.position.y
        }
    }
}















