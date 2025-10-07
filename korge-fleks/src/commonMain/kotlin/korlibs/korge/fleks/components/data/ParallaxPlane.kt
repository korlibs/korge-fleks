package korlibs.korge.fleks.components.data

import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This class is used to ...
 */
@Serializable @SerialName("ParallaxPlane")
class ParallaxPlane private constructor(
    var name: String = "",
    var entity: Entity = Entity.NONE,
    // Used for horizontal or vertical movements of line and attached layers depending on ParallaxMode
    val linePositions: MutableList<Float> = mutableListOf(),
    val bottomAttachedLayerPositions: MutableList<Float> = mutableListOf(),
    val topAttachedLayerPositions: MutableList<Float> = mutableListOf()
) : Poolable<ParallaxPlane> {
    // Init an existing data instance with data from another one
    override fun init(from: ParallaxPlane) {
        name = from.name
        entity = from.entity
        // Make deep copy of the line and layer positions - they are changing
        linePositions.addAll(from.linePositions)
        bottomAttachedLayerPositions.addAll(from.bottomAttachedLayerPositions)
        topAttachedLayerPositions.addAll(from.topAttachedLayerPositions)
    }

    // Cleanup data instance manually
    // This is used for data instances when they are a value property of a component
    override fun cleanup() {
        name = ""
        entity = Entity.NONE
        linePositions.clear()
        bottomAttachedLayerPositions.clear()
        topAttachedLayerPositions.clear()
    }

    // Clone a new data instance from the pool
    override fun clone(): ParallaxPlane = parallaxPlane { init(from = this@ParallaxPlane) }

    // Cleanup the tween data instance manually
    override fun free() {
        cleanup()
        pool.free(this)
    }

    companion object {
        // Use this function to create a new instance of data as value property inside a component
        fun staticParallaxPlane(config: ParallaxPlane.() -> Unit): ParallaxPlane =
            ParallaxPlane().apply(config)

        // Use this function to get a new instance of the data object from the pool
        fun parallaxPlane(config: ParallaxPlane.() -> Unit): ParallaxPlane =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "ParallaxPlane") { ParallaxPlane() }
    }
}
