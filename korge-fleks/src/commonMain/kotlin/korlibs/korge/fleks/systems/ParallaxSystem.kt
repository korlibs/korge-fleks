package korlibs.korge.fleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.assets.data.ClusterAssetInfo.ParallaxLayersInfo.ParallaxLayer
import korlibs.korge.fleks.assets.data.ClusterAssetInfo.ParallaxLayersInfo.Mode.*
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
        val parallaxConfig = assetStore.getParallaxLayers(parallaxComponent.name)

        // Update local positions for each layer which is configured to be moving (speedFactor not null or zero)
        parallaxConfig.backgroundLayers.forEach { layer -> update(layer, parallaxComponent.getBgLayerEntity(layer.name), motionComponent) }
        parallaxConfig.foregroundLayers.forEach { layer -> update(layer, parallaxComponent.getFgLayerEntity(layer.name), motionComponent) }

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
                val speedFactor = parallaxPlane.topAttachedLayers[index].speedFactor
                // Update position of parallax position
                val layerLength = when (parallaxConfig.mode) {
                    HORIZONTAL_PLANE -> {
                        parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                        parallaxPlane.topAttachedLayers[index].bmpSlice.width
                    }
                    VERTICAL_PLANE -> {
                        parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                        parallaxPlane.topAttachedLayers[index].bmpSlice.height
                    }
                    else -> 0
                }
                // Check layer positions and wrap around the texture size
                parallaxComponent.parallaxPlane.topAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.topAttachedLayerPositions[index], max = layerLength)
            }

            // Update bottom-attached layers of parallax plane
            parallaxComponent.parallaxPlane.bottomAttachedLayerPositions.forEachIndexed { index, layerPosition ->
                val speedFactor = parallaxPlane.bottomAttachedLayers[index].speedFactor
                // Update position of parallax position
                val layerLength = when (parallaxConfig.mode) {
                    HORIZONTAL_PLANE -> {
                        parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityX * worldToPixelRatio) * deltaTime + layerPosition
                        parallaxPlane.bottomAttachedLayers[index].bmpSlice.width
                    }
                    VERTICAL_PLANE -> {
                        parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = (speedFactor * motionComponent.velocityY * worldToPixelRatio) * deltaTime + layerPosition
                        parallaxPlane.bottomAttachedLayers[index].bmpSlice.height
                    }
                    else -> 0
                }
                // Check layer positions and wrap around the texture size
                parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index] = wrap(parallaxComponent.parallaxPlane.bottomAttachedLayerPositions[index], max = layerLength)
            }
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
