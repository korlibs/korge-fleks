package korlibs.korge.fleks.components.data.tweenSequence


import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("SpawnEntity")
class SpawnEntity private constructor(
    var entityConfig: String = "",      // name of the entity configuration which creates and configures the spawned entity
    var x: Float = 0f,                  // position where entity will be spawned
    var y: Float = 0f,

    override var target: Entity = Entity.NONE, // when entity is not given (= Entity.NONE) than it will be created
    override var delay: Float? = null,
    override var duration: Float? = 0f,    // not used - 0f for immediately
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    // Init an existing tween data instance with data from another tween
    fun init(from: SpawnEntity) {
        entityConfig = from.entityConfig
        x = from.x
        y = from.y
        target = from.target
        delay = from.delay
        duration = from.duration
        easing = from.easing
        // Hint: it is not needed to copy "easing" property by creating new one like below:
        // easing = Easing.ALL[easing::class.toString().substringAfter('$')]
    }

    // Cleanup the tween data instance manually
    override fun free() {
        entityConfig = ""
        x = 0f
        y = 0f
        target = Entity.NONE
        delay = null
        duration = null
        easing = null

        pool.free(this)
    }

    companion object {
        // Use this function to get a new instance of a tween from the pool and add it to the tweens list of a component or sub-list
        fun TweenListBase.spawnEntity (config: SpawnEntity.() -> Unit) {
            tweens.add(pool.alloc().apply(config))
        }

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "SpawnEntity") { SpawnEntity() }
    }
}
