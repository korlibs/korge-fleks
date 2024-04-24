package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.datastructure.iterators.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.components.*
import korlibs.korge.parallax.ParallaxConfig.Mode.*


class ParallaxSystem(
    private val worldToPixelRatio: Float
) : IteratingSystem(
    family = family { all(ParallaxComponent, MotionComponent) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val (config, backgroundLayers, attachedLayersRear, parallaxPlane, attachedLayersFront, foregroundLayers) = entity[ParallaxComponent]
        val (_, _, velocityX, velocityY) = entity[MotionComponent]
        val parallaxDataContainer = AssetStore.getBackground(config.name)

        // Update local positions for each layer which is configured to be moving (speedFactor not null or zero)
        backgroundLayers.fastForEachWithIndex { index, layer ->
            val speedFactor = parallaxDataContainer.config.backgroundLayers!![index].speedFactor
            if (speedFactor != null) {
                layer.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + layer.position.x
                layer.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + layer.position.y
            }
        }

        attachedLayersRear.fastForEachWithIndex { index, layer ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersRear!!.defaultAnimation.firstFrame.layerData[index]
            val globalOffset = plane.offset
            val attachTextureOffset =
                if (plane.attachedLayersRear!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax layer
                layer.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + layer.position.x
            } else if (parallaxMode == VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax layer
                layer.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + layer.position.y
            }
        }

        parallaxPlane.fastForEachWithIndex { index, line ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val offset = plane.offset
            if (parallaxMode == HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetY - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                line.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + line.position.x
            } else if (parallaxMode == VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = parallaxDataContainer.parallaxPlane!!.imageDatas[index].defaultAnimation.firstFrame.targetX - offset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax plane line
                line.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + line.position.y
            }
        }

        attachedLayersFront.fastForEachWithIndex { index, layer ->
            val plane = parallaxDataContainer.config.parallaxPlane!!
            val parallaxMode = parallaxDataContainer.config.mode
            val texture = parallaxDataContainer.attachedLayersFront!!.defaultAnimation.firstFrame.layerData[index]
            val globalOffset = plane.offset
            val attachTextureOffset = if (plane.attachedLayersFront!![index].attachBottomRight) texture.height else 0
            if (parallaxMode == HORIZONTAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetY - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax layer
                layer.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + layer.position.x
            } else if (parallaxMode == VERTICAL_PLANE) {
                // Get correct speed factor for parallax plane lines from FloatArray
                val lineIndex = texture.targetX - globalOffset + attachTextureOffset
                val speedFactor = plane.parallaxPlaneSpeedFactors[lineIndex]
                // Update position of parallax layer
                layer.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + layer.position.y
            }
        }

        foregroundLayers.fastForEachWithIndex { index, layer ->
            val speedFactor = parallaxDataContainer.config.foregroundLayers!![index].speedFactor
            if (speedFactor != null) {
                layer.position.x = (speedFactor * velocityX * worldToPixelRatio) * deltaTime + layer.position.x
                layer.position.y = (speedFactor * velocityY * worldToPixelRatio) * deltaTime + layer.position.y
            }
        }
    }
}
