package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.format.ImageAnimation.Direction
import korlibs.image.format.ImageAnimation.Direction.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.components.Rgba.Companion.RgbaComponent
import korlibs.korge.fleks.components.data.TextureRef
import korlibs.korge.fleks.components.data.TextureRef.Companion.free
import korlibs.korge.fleks.components.data.TextureRef.Companion.init
import korlibs.korge.fleks.components.data.TextureRef.Companion.textureRef
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Sprite component with full control over texture layers.
 *
 * @param layerMap is used in various animation systems to control properties of a sprite layer.
 * @param layerList is used to render all layers in a specific order.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
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
) : PoolableComponent<LayeredSprite>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: LayeredSprite) {
        name = from.name
        anchorX = from.anchorX
        anchorY = from.anchorY
        animation = from.animation
        frameIndex = from.frameIndex
        running = from.running
        direction = from.direction
        destroyOnAnimationFinished = from.destroyOnAnimationFinished

        // internal
        increment = from.increment
        nextFrameIn = from.nextFrameIn

        // Perform deep copy of all texture layer objects in the list
        layerList.init(from.layerList)
        // Map layers from list by name into the map (do not copy but use references to the same objects)
        layerList.forEach { layer ->
            layerMap[layer.name] = layer
        }
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
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
        layerList.free()
        // We only need to clear the map since the objects were already put back to the pool by above list cleanup
        layerMap.clear()
    }

    override fun type() = LayeredSpriteComponent

    companion object {
        val LayeredSpriteComponent = componentTypeOf<LayeredSprite>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticLayeredSpriteComponent(config: LayeredSprite.() -> Unit ): LayeredSprite =
        LayeredSprite().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun layeredSpriteComponent(config: LayeredSprite.() -> Unit ): LayeredSprite =
        pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE) { LayeredSprite() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): LayeredSprite = layeredSpriteComponent { init(from = this@LayeredSprite ) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
        // Initialize animation properties with data from [AssetStore].
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
                // Get a new instance from the TextureRefData pool
                val layer = textureRef { name = data.layer.name!! }
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

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
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
        // Cleanup the layerMap to put the TextureRef object back to the pool
        layerMap.free()
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
            layerMap[layer.name] = layer
        }
    }
}
