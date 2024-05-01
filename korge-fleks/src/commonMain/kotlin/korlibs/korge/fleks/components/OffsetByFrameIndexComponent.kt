package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.config.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.*


/**
 * This component can store different offsets for each animation frame of a sprite.
 *
 * @param mapOfOffsetLists A map which saves a list of offsets (values) for each animation frame per animation name (keys).
 */
@Serializable
@SerialName("OffsetByFrameIndex")
data class OffsetByFrameIndexComponent(
    var entity: Entity = Entity.NONE,     // TODO remove - it will not be needed with new sprite render view system - frame index is coming from SpriteAnimationComponent
    var mapOfOffsetLists: Map<String, List<Point>> = emptyMap()
) : Component<OffsetByFrameIndexComponent> {

    @Serializable
    @SerialName("Point")
    data class Point(var x: Float = 0f, var y: Float = 0f) : SerializeBase

    override fun type(): ComponentType<OffsetByFrameIndexComponent> = OffsetByFrameIndexComponent
    companion object : ComponentType<OffsetByFrameIndexComponent>()
}
