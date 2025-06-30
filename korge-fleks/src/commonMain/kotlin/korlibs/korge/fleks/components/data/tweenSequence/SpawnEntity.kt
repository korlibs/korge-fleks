package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This Tween is used to spawn a new entity in the world.
 * The entity will be created with the given entity configuration and positioned at the given coordinates.
 */
@Serializable @SerialName("SpawnEntity")
class SpawnEntity private constructor(
    var entityConfig: String = "",      // name of the entity configuration which creates and configures the spawned entity
    var x: Float = 0f,                  // position where entity will be spawned
    var y: Float = 0f,

    override var target: Entity = Entity.NONE, // when entity is not given (= Entity.NONE) than it will be created
    override var delay: Float? = null,
    override var duration: Float? = 0f,    // not used - 0f for immediately
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase, Poolable<SpawnEntity> {
    // Init an existing data instance with data from another one
    override fun init(from: SpawnEntity) {
        entityConfig = from.entityConfig
        x = from.x
        y = from.y

        target = from.target
        delay = from.delay
        // duration not used
        // easing not used
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        entityConfig = ""
        x = 0f
        y = 0f

        target = Entity.NONE
        delay = null
        // duration not used
        // easing not used
    }

    // Clone a new data instance from the pool
    override fun clone(): SpawnEntity = pool.alloc().apply { init(from = this@SpawnEntity ) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticSpawnEntity(config: SpawnEntity.() -> Unit ): SpawnEntity =
            SpawnEntity().apply(config)

        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.spawnEntity(config: SpawnEntity.() -> Unit ) { tweens.add(pool.alloc().apply(config)) }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SpawnEntity") { SpawnEntity() }
    }
}
