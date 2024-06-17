package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
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
    var mapOfOffsetLists: Map<String, List<Point>> = emptyMap()
) : CloneableComponent<OffsetByFrameIndexComponent>() {

    @Serializable @SerialName("Point")
    data class Point(
        var x: Float = 0f,
        var y: Float = 0f
    ) : CloneableData<Point> {

        override fun clone(): Point = this.copy()
    }

    override fun type(): ComponentType<OffsetByFrameIndexComponent> = OffsetByFrameIndexComponent
    companion object : ComponentType<OffsetByFrameIndexComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): OffsetByFrameIndexComponent =
        this.copy(
            // Perform deep copy of Entity and map
            entity = entity.clone(),
            mapOfOffsetLists = mapOfOffsetLists.clone()
        )
}
