package korlibs.korge.fleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("LifeCycle")
data class LifeCycleComponent(
    var healthCounter: Int = 100
) : Component<LifeCycleComponent> {
    override fun type(): ComponentType<LifeCycleComponent> = LifeCycleComponent
    companion object : ComponentType<LifeCycleComponent>()

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : LifeCycleComponent = this.copy()
}
