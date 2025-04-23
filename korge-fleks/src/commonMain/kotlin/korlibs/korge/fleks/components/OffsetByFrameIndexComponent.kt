package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.data.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This component can store different offsets for each animation frame of a sprite.
 *
 * @param mapOfOffsetLists A map which saves a list of offsets (values) for each animation frame per animation name (keys).
 */
@Serializable @SerialName("OffsetByFrameIndex")
data class OffsetByFrameIndexComponent(
    var entity: Entity = Entity.NONE,
    var mapOfOffsetLists: Map<String, List<PointOld>> = mutableMapOf()
) : CloneableComponent<OffsetByFrameIndexComponent>() {
    override fun type(): ComponentType<OffsetByFrameIndexComponent> = OffsetByFrameIndexComponent
    companion object : ComponentType<OffsetByFrameIndexComponent>()

//    TODO: For later use
//    companion object {
//        val OffsetByFrameIndexComponent = componentTypeOf<OffsetByFrameIndexComponent>()
//
//        fun World.OffsetByFrameIndexComponent(config: OffsetByFrameIndexComponent.() -> Unit ): OffsetByFrameIndexComponent =
//            getPoolable(OffsetByFrameIndexComponent).apply { config() }
//
//        fun InjectableConfiguration.addOffsetByFrameIndexComponentPool(preAllocate: Int = 0) {
//            addPool(OffsetByFrameIndexComponent, preAllocate) { OffsetByFrameIndexComponent() }
//        }
//    }

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): OffsetByFrameIndexComponent =
        this.copy(
            // Perform deep copy of Entity and map
            entity = entity.clone(),
            mapOfOffsetLists = mapOfOffsetLists.clone()
        )
}
