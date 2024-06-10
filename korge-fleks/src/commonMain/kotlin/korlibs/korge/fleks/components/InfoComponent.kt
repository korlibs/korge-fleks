package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable @SerialName("Info")
data class InfoComponent(
    var name: String = "noName",
    var entityId: Int = -1,

    // internal
    var initialized: Boolean = false
) : Component<InfoComponent> {
    override fun type(): ComponentType<InfoComponent> = InfoComponent

    override fun World.onAdd(entity: Entity) {
        // Make sure that initialization is skipped on world snapshot loading (deserialization of save game)
        if (initialized) return
        else initialized = true

        entityId = entity.id
        EntityByName.add(name, entity)
    }

    override fun World.onRemove(entity: Entity) {
        EntityByName.remove(name)
    }

    companion object : ComponentType<InfoComponent>()

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : InfoComponent = this.copy()
}

