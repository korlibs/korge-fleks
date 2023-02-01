package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.*

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
data class TiledMap(
    var assetName: String = "",
) : Component<TiledMap> {
    override fun type(): ComponentType<TiledMap> = TiledMap
    companion object : ComponentType<TiledMap>() {
        /**
         * Hint: How you should read "Component Hook"
         *
         * A component listener creates and initializes the internal object(s) with all parameters of the component.
         * This initialization can only use data from one component. Thus, it must be self-containable regarding the
         * save-state of an object. But this is often not the case -> check FamilyHooks.
         */
        fun onComponentAdded(entity: Entity, component: TiledMap) {}

        fun onComponentRemoved(entity: Entity, component: TiledMap) {}
    }
}
