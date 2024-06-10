package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Hint: How you should read "Component"
 *
 * Parameters of a component saves the state of an entity which uses this component.
 * The internal objects will be updated every cycle with all parameters. This makes sure
 * that the internal object can be restored at any time from a saved state.
 *
 * For serialize and deserialize of component data all properties shall be of basic types.
 * Korge-specific objects shall not be added to any component. They will be stored
 * in Korge specific cache objects and used in systems.
 */
@Serializable
@SerialName("TiledLevelMap")
data class TiledLevelMapComponent(
    var assetName: String = "",
) : Component<TiledLevelMapComponent> {
    override fun type(): ComponentType<TiledLevelMapComponent> = TiledLevelMapComponent
    companion object : ComponentType<TiledLevelMapComponent>()

    // Hint to myself: Check if deep copy is needed on any change in the component!
    fun clone() : TiledLevelMapComponent = this.copy()
}
