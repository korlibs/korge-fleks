package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component can store different offsets for each animation frame of a sprite.
 *
 * @param mapOfOffsetLists A map which saves a list of offsets (values) for each animation frame per animation name (keys).
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("OffsetByFrameIndex")
class OffsetByFrameIndex private constructor(
    var entity: Entity = Entity.NONE,
    // TODO: Use a mutableMap of "components with a mutableList of points" instead of a map-to-list-of-points
    var mapOfOffsetLists: Map<String, List<Point>> = mapOf()
) : Poolable<OffsetByFrameIndex>() {
    override fun type() = OffsetByFrameIndexComponent

    companion object {
        val OffsetByFrameIndexComponent = componentTypeOf<OffsetByFrameIndex>()

        // Use this function to create a new instance as val inside another component
        fun staticOffsetByFrameIndexComponent(config: OffsetByFrameIndex.() -> Unit ): OffsetByFrameIndex =
            OffsetByFrameIndex().apply(config)

        // Use this function to get a new instance from the pool
        fun World.OffsetByFrameIndexComponent(config: OffsetByFrameIndex.() -> Unit ): OffsetByFrameIndex =
        getPoolable(OffsetByFrameIndexComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addOffsetByFrameIndexComponentPool(preAllocate: Int = 0) {
            addPool(OffsetByFrameIndexComponent, preAllocate) { OffsetByFrameIndex() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): OffsetByFrameIndex =
    getPoolable(OffsetByFrameIndexComponent).apply { init(from = this@OffsetByFrameIndex ) }

    // Init an existing component instance with data from another component
    fun init(from: OffsetByFrameIndex) {
        entity = from.entity
        mapOfOffsetLists = from.mapOfOffsetLists
    }

    // Cleanup the component instance manually
    fun cleanup() {
        entity = Entity.NONE
        // Lists of Points are static and will be freed to the pool in cleanupComponent function when entity is destroyed
        mapOfOffsetLists = mapOf()
    }

    // Cleanup the component automatically when it is removed from an entity
    override fun World.cleanupComponent(entity: Entity) {
        this@OffsetByFrameIndex.entity = Entity.NONE
        // Put all points back to the pool
        mapOfOffsetLists.forEach { (_, list) ->
            list.forEach { point ->
                point.run { this@cleanupComponent.free() }
            }
        }
        mapOfOffsetLists = mapOf()
    }
}
