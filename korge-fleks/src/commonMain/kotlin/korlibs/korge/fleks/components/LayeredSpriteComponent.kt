package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.datastructure.iterators.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.assetmanager.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*

/**
 * Sprite component with full control over texture layers.
 *
 *
 * @param layerMap is used in various animation systems to control properties of a sprite layer.
 * @param layerList is used to render all layers in a specific order.
 */
@Serializable @SerialName("LayeredSprite")
data class LayeredSpriteComponent(
    var name: String = "",
    var anchorX: Float = 0f,                          // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,

    var animation: String? = null,                    // Leave null if sprite texture does not have an animation
    var frameIndex: Int = 0,                          // frame number of animation which is currently drawn
    var running: Boolean = false,                     // Switch animation on and off
    var direction: ImageAnimation.Direction? = null,
    var destroyOnAnimationFinished: Boolean = false,  // Delete entity when direction is [ONCE_FORWARD] or [ONCE_REVERSE]

    // internal, do not set directly
    var increment: Int = -2,                          // out of [-1, 0, 1]; will be added to frameIndex each new frame
    var nextFrameIn: Float = 0f,                      // time in seconds until next frame of animation shall be shown
    var initialized: Boolean = false,

    // internally used for rendering and tween animation of layer position and rgba (alpha channel)
    var layerList: List<Layer> = listOf(),
    var layerMap: Map<String, Layer> = mapOf()
): CloneableComponent<LayeredSpriteComponent>() {

    @Serializable @SerialName("LayeredSprite.Layer")
    data class Layer(
        val name: String,
        var entity: Entity = Entity.NONE,  // Link to entity for tween animation
        /**
         * Local position of layer relative to the top-left point of the parallax entity (global PositionComponent).
         */
        val position: PositionComponent = PositionComponent(),
        val rgba: RgbaComponent = RgbaComponent()
    ) : CloneableData<Layer> {

        // Perform deep copy with special handling for entity, position and rgba.
        override fun clone(): Layer =
            this.copy(
                entity = entity.clone(),
                position = position.clone(),
                rgba = rgba.clone()
            )
    }

    // Set frameIndex for starting animation
    fun setFrameIndex(assetStore: AssetStore) {
        frameIndex = if (direction == REVERSE || direction == ONCE_REVERSE)
            assetStore.getAnimationNumberOfFrames(name, animation) - 1 else 0
    }

    // Set frame time for first frame
    fun setNextFrameIn(assetStore: AssetStore) {
        nextFrameIn = assetStore.getAnimationFrameDuration(name, animation, frameIndex)
    }

    // Init increment for setting frameIndex
    fun setIncrement() {
        increment = when (direction) {
            FORWARD -> +1
            REVERSE -> -1
            PING_PONG -> +1     // ping-pong is starting forward
            ONCE_FORWARD -> +1  // starting forward
            ONCE_REVERSE -> -1  // starting reverse
            null -> error("SpriteAnimationFamily: direction shall not be null!")
        }
    }

    /**
     * Initialize animation properties with data from [AssetStore].
     */
    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        val assetStore: AssetStore = this.inject(name = "AssetStore")

        // Set direction from Aseprite if not specified in the component
        if (direction == null) {
            direction = assetStore.getAnimationDirection(name, animation)
        }
        setFrameIndex(assetStore)
        setNextFrameIn(assetStore)
        setIncrement()

        // Create map and list of all layers of the sprite textures
        val map = mutableMapOf<String, Layer>()
        val list = mutableListOf<Layer>()
        assetStore.getImageAnimation(name, animation).firstFrame.layerData.forEach { data ->
            if (data.layer.name != null) {
                val layer = Layer(name = data.layer.name!!)
                map[data.layer.name!!] = layer
                list.add(layer)
            }
        }
        layerMap = map
        layerList = list

        // Create new entities for controlling position and color of each layer e.g. by the TweenEngineSystem
        layerList.forEach { layer ->
            // Create new entity and add existing components from the sprite layer config
            layer.entity = this.entity("Sprite layer '${layer.name}' of entity '${entity.id}'") {
                it += layer.position
                it += layer.rgba
            }
            println("create entity '${layer.entity.id}' for layer '${layer.name}'")
        }

//        println("\nSpriteAnimationComponent:\n    entity: ${entity.id}\n    numFrames: $numFrames\n    increment: ${spriteAnimationComponent.increment}\n    direction: ${spriteAnimationComponent.direction}\n")
    }

    /**
     * After deserialization some cleanup and setup needs to be done
     * go through each layer entity and copy the Position and RgbaComponent from the layer lists.
     */
    fun World.updateLayerEntities() {
        // Overwrite existing components with those from the layer config list
        val newMap = mutableMapOf<String, Layer>()
        layerList.forEach { layer ->
            layer.entity.configure {
                it += layer.position
                it += layer.rgba
            }
            newMap[layer.name] = layer
        }
        layerMap = newMap
    }

    override fun type() = LayeredSpriteComponent
    companion object : ComponentType<LayeredSpriteComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LayeredSpriteComponent =
        this.copy(
            // Perform deep copy
            direction = direction,  // normal ordinary enum - no deep copy needed
            layerList = layerList.clone(),
            layerMap = layerMap.clone()
        )
}
