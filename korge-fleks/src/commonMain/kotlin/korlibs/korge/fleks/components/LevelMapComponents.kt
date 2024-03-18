package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
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
    companion object : ComponentType<TiledLevelMapComponent>() {
        /**
         * Hint: How you should read "Component Hook"
         *
         * A component listener creates and initializes the internal object(s) with all parameters of the component.
         * This initialization can only use data from one component. Thus, it must be self-containable regarding the
         * save-state of an object. But this is often not the case -> check FamilyHooks.
         */
        // Not used here
        // fun onComponentAdded(entity: Entity, component: TiledLevelMapComponent) {}
        // fun onComponentRemoved(entity: Entity, component: TiledLevelMapComponent) {}
    }
}

@Serializable
@SerialName("LdtkLevelMap")
data class LdtkLevelMapComponent(
    /**
     * The name of the LDtk world in the Asset manager
     */
    var worldName: String = "",
    /**
     * The unique identifier (level name) of the level from the LDtk world
     */
    var levelName: String = ""

) : Component<LdtkLevelMapComponent> {
    override fun type(): ComponentType<LdtkLevelMapComponent> = LdtkLevelMapComponent
    companion object : ComponentType<LdtkLevelMapComponent>()
}
