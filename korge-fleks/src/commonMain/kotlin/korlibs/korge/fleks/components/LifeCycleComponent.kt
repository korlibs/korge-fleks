package korlibs.korge.fleks.components

import com.github.quillraven.fleks.ComponentType
import korlibs.korge.fleks.utils.componentPool.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable @SerialName("LifeCycle")
data class LifeCycleComponent(
    var healthCounter: Int = 100
) : PoolableComponent1<LifeCycleComponent>() {
    override fun type(): ComponentType<LifeCycleComponent> = LifeCycleComponent
    companion object : ComponentType<LifeCycleComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LifeCycleComponent = this.copy()
}
