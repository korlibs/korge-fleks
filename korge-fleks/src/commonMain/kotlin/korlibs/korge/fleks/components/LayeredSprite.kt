package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.format.ImageAnimation.Direction.FORWARD
import korlibs.image.format.ImageAnimation.Direction.REVERSE
import korlibs.image.format.ImageAnimation.Direction.PING_PONG
import korlibs.image.format.ImageAnimation.Direction.ONCE_FORWARD
import korlibs.image.format.ImageAnimation.Direction.ONCE_REVERSE
import korlibs.image.format.ImageAnimation.Direction
import korlibs.korge.fleks.assets.*
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.data.*
import korlibs.korge.fleks.components.data.TextureRef.Companion.TextureRefData
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Sprite component with full control over texture layers.
 *
 * @param layerMap is used in various animation systems to control properties of a sprite layer.
 * @param layerList is used to render all layers in a specific order.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("LayeredSprite")
class LayeredSprite private constructor(
    var name: String = "",
    var anchorX: Float = 0f,                          // x,y position of the pivot point within the sprite
    var anchorY: Float = 0f,

    var animation: String? = null,                    // Leave null if sprite texture does not have an animation
    var frameIndex: Int = 0,                          // frame number of animation which is currently drawn
    var running: Boolean = false,                     // Switch animation on and off
    var direction: Direction? = null,
    var destroyOnAnimationFinished: Boolean = false,  // Delete entity when direction is [ONCE_FORWARD] or [ONCE_REVERSE]

    // internal, do not set directly
    var increment: Int = -2,                          // out of [-1, 0, 1]; will be added to frameIndex each new frame
    var nextFrameIn: Float = 0f,                      // time in seconds until next frame of animation shall be shown

    // internally used for rendering and tween animation of texture layer position and rgba (alpha channel)
    val layerList: MutableList<TextureRef> = mutableListOf(),
    val layerMap: MutableMap<String, TextureRef> = mutableMapOf()
) : Poolable<LayeredSprite>() {
    override fun type() = LayeredSpriteComponent

    companion object {
        val LayeredSpriteComponent = componentTypeOf<LayeredSprite>()

        fun World.LayeredSpriteComponent(config: LayeredSprite.() -> Unit ): LayeredSprite =
            getPoolable(LayeredSpriteComponent).apply { config() }

        fun InjectableConfiguration.addLayeredSpriteComponentPool(preAllocate: Int = 0) {
            addPool(LayeredSpriteComponent, preAllocate) { LayeredSprite() }
        }
    }

    override fun World.clone(): LayeredSprite =
        getPoolable(LayeredSpriteComponent).apply {
            name = this@LayeredSprite.name
            anchorX = this@LayeredSprite.anchorX
            anchorY = this@LayeredSprite.anchorY
            animation = this@LayeredSprite.animation
            frameIndex = this@LayeredSprite.frameIndex
            running = this@LayeredSprite.running
            direction = this@LayeredSprite.direction
            destroyOnAnimationFinished = this@LayeredSprite.destroyOnAnimationFinished

            // internal
            increment = this@LayeredSprite.increment
            nextFrameIn = this@LayeredSprite.nextFrameIn

            // Perform deep copy of all texture layer objects in the list
            layerList.init(world = this@clone, from = this@LayeredSprite.layerList)
            // Map layers from list by name into the map (do not copy but use references to the same objects)
            layerList.forEach { layer ->
                layerMap[layer.name] = layer
            }
        }

    /**
     * Initialize animation properties with data from [AssetStore].
     */
    override fun World.initComponent(entity: Entity) {
        val assetStore: AssetStore = this.inject(name = "AssetStore")

        // Set direction from Aseprite if not specified in the component
        if (direction == null) {
            direction = assetStore.getAnimationDirection(name, animation)
        }
        setFrameIndex(assetStore)
        setNextFrameIn(assetStore)
        setIncrement()

        // populate map and list of all layers of the sprite textures
        assetStore.getImageAnimation(name, animation).firstFrame.layerData.forEach { data ->
            if (data.layer.name != null) {
                val layer = this.run {
                    // Get a new instance from the TextureRefData pool
                    TextureRefData { name = data.layer.name!! }
                }
                // Store reference to same layer data object in list and map
                layerMap[data.layer.name!!] = layer
                layerList.add(layer)
            }
        }

        // Create new entities for controlling position and color of each layer e.g. by the TweenEngineSystem
        layerList.forEach { layer ->
            // Create new entity and add existing components from the sprite layer config
            layer.entity = this.entity("Sprite layer '${layer.name}' of entity '${entity.id}'") {
                it += layer.position
                it += layer.rgba
            }
            println("create entity '${layer.entity.id}' for layer '${layer.name}'")
        }
        //println("\nSpriteAnimationComponent:\n    entity: ${entity.id}\n    numFrames: $numFrames\n    increment: ${spriteAnimationComponent.increment}\n    direction: ${spriteAnimationComponent.direction}\n")
    }

    override fun World.cleanupComponent(entity: Entity) {
        name = ""
        anchorX = 0f
        anchorY = 0f
        animation = null
        frameIndex = 0
        running = false
        direction = null
        destroyOnAnimationFinished = false

        increment = -2
        nextFrameIn = 0f

        // Put back all layers TextureRef objects to the pool
        layerList.cleanup(world = this)
        // We only need to clear the map since the objects were already put back to the pool by above list cleanup
        layerMap.clear()
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
     * After deserialization some cleanup and setup needs to be done
     * go through each layer entity and copy the Position and RgbaComponent from the layer lists.
     */
    fun World.updateLayerEntities() {
        // Overwrite existing components with those from the layer config list
        layerList.forEach { layer ->
            layer.entity.configure {
                // Remove existing components - this triggers cleanup and freeing to component pool
                it -= PositionComponent
                it -= RgbaComponent
                // Add existing components from the TextureRef object - they need to be a reference to the same component objects
                it += layer.position
                it += layer.rgba
            }
            // Cleanup the layerMap to put the TextureRef object back to the pool
            layerMap.cleanup(world = this)
            layerMap[layer.name] = layer
        }
    }
}

/*
@Serializable @SerialName("$FLEKS_COMPONENT$")
class $FLEKS_COMPONENT$ private constructor(
var answer: Int = 42
) : Poolable<$FLEKS_COMPONENT$>() {
    override fun type() = $FLEKS_COMPONENT$Component

    companion object {
        val $FLEKS_COMPONENT$Component = componentTypeOf<$FLEKS_COMPONENT$>()

        // Use this function to create a new instance as val inside another component
        fun static$FLEKS_COMPONENT$Component(config: $FLEKS_COMPONENT$.() -> Unit ): $FLEKS_COMPONENT$ =
            $FLEKS_COMPONENT$().apply(config)

        // Use this function to get a new instance from the pool
        fun World.$FLEKS_COMPONENT$Component(config: $FLEKS_COMPONENT$.() -> Unit ): $FLEKS_COMPONENT$ =
        getPoolable($FLEKS_COMPONENT$Component).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.add$FLEKS_COMPONENT$ComponentPool(preAllocate: Int = 0) {
            addPool($FLEKS_COMPONENT$Component, preAllocate) { $FLEKS_COMPONENT$() }
        }
    }

    // Create a new instance of the component from the pool
    override fun World.clone(): $FLEKS_COMPONENT$ =
    getPoolable($FLEKS_COMPONENT$Component).apply { init(from = this@$FLEKS_COMPONENT$ ) }

    // Init an existing component instance with data from another component
    fun init(from: $FLEKS_COMPONENT$) {
        answer = from.answer
    }

    // Cleanup the component instance manually
    fun cleanup() {
        answer = 42
    }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }
}
*/