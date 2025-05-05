package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component enables rendering objects in layers.
 *
 * @param [layerIndex] defines the order in which objects like textures or shapes will be drawn.
 * Higher numbers mean that the object will be rendered on top of other objects with smaller number.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Layer")
class Layer private constructor(
    var layerIndex: Int = 0
) : Poolable<Layer>() {
    override fun type() = LayerComponent

    companion object {
        val LayerComponent = componentTypeOf<Layer>()

        fun World.LayerComponent(config: Layer.() -> Unit ): Layer =
            getPoolable(LayerComponent).apply { config() }

        fun InjectableConfiguration.addLayerComponentPool(preAllocate: Int = 0) {
            addPool(LayerComponent, preAllocate) { Layer() }
        }
    }

    override fun World.clone(): Layer =
        getPoolable(LayerComponent).apply {
            layerIndex = this@Layer.layerIndex
        }

    override fun World.cleanupComponent(entity: Entity) {
        layerIndex = 0
    }
}
