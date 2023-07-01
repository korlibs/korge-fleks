package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("SubEntities")
data class SubEntities(
    var entities: MutableMap<String, Entity> = mutableMapOf(),
    var moveWithParent: Boolean = true
) : Component<SubEntities>, SerializeBase {
    override fun type(): ComponentType<SubEntities> = SubEntities
    companion object : ComponentType<SubEntities>()

    operator fun get(name: String) : Entity = entities[name] ?: error("SubEntities: Entity with name '$name' not found!")
}
