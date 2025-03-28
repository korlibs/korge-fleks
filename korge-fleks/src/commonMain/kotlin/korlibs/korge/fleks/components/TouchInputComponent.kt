package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("TouchInput")
data class TouchInputComponent(
    var enabled: Boolean = true,
    var entity: Entity = Entity.NONE,  // If touch was triggered than below EntityConfig will be executed for this Entity
    var entityConfig: String = "",
    var passPositionToEntity: Boolean = false,
    var continuousTouch: Boolean = false
) : PoolableComponent1<TouchInputComponent>() {
    override fun type(): ComponentType<TouchInputComponent> = TouchInputComponent
    companion object : ComponentType<TouchInputComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): TouchInputComponent =
        this.copy(
            entity = entity.clone()
        )
}
