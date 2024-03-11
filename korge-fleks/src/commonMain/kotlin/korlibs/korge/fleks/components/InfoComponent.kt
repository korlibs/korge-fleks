package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable @SerialName("Info")
data class InfoComponent(
    var name: String = "noName",
    var entityId: Int = -1
) : Component<InfoComponent> {
    override fun type(): ComponentType<InfoComponent> = InfoComponent

    override fun World.onAdd(entity: Entity) {
        entityId = entity.id
        EntityByName.add(name, entity)
    }

    override fun World.onRemove(entity: Entity) {
        EntityByName.remove(name)
    }

    companion object : ComponentType<InfoComponent>()
}