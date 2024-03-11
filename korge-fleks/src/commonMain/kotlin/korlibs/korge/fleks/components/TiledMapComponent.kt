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
 * in the Korge specific systems.
 */
@Serializable
@SerialName("TiledMap")
data class TiledMapComponent(
    var assetName: String = "",
) : Component<TiledMapComponent> {
    override fun type(): ComponentType<TiledMapComponent> = TiledMapComponent
    companion object : ComponentType<TiledMapComponent>() {
        /**
         * Hint: How you should read "Component Hook"
         *
         * A component listener creates and initializes the internal object(s) with all parameters of the component.
         * This initialization can only use data from one component. Thus, it must be self-containable regarding the
         * save-state of an object. But this is often not the case -> check FamilyHooks.
         */
        fun onComponentAdded(entity: Entity, component: TiledMapComponent) {}

        fun onComponentRemoved(entity: Entity, component: TiledMapComponent) {}
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