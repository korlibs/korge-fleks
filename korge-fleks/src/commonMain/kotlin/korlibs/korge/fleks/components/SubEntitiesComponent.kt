package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("SubEntities")
data class SubEntitiesComponent(
    var entities: MutableMap<String, Entity> = mutableMapOf(),
    var moveWithParent: Boolean = true
) : Component<SubEntitiesComponent> {
    override fun type(): ComponentType<SubEntitiesComponent> = SubEntitiesComponent
    companion object : ComponentType<SubEntitiesComponent>()

    operator fun get(name: String) : Entity = entities[name] ?: error("SubEntities: Entity with name '$name' not found!")
}
