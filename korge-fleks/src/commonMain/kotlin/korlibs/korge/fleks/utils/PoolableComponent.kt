package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.gameState.*


/**
 * All components needs to be derived from [PoolableComponent] to be able to be reused from a Component pool and
 * to be recorded in the SnapshotSerializerSystem.
 *
 * The reset function is called when the component is going to be reused for a new entity.
 * The clone function needs to be implemented to perform a deep copy of all properties of the component for
 * the serialization of the game state.
 *
 * Note:
 *   - The initComponent and cleanupComponent functions are called as normal life-cycle functions of the component.
 *     They are not called when the snapshot rewind/forward feature is used because the components are already
 *     initialized when loaded (i.e. deserialized) from a snapshot.
 *   - The initPrefabs and cleanupPrefabs functions are called always when the component is added to or removed from a world.
 *     Also during the snapshot rewind/forward feature.
 */
abstract class PoolableComponent<T> : Component<T> {
    /**
     * Use this function to initialize a complex entity which might have sub-entities or similar.
     */
    open fun World.initComponent(entity: Entity) = Unit

    /**
     * Use this function to clean up/reset a complex entity which might have sub-entities or similar.
     */
    open fun World.cleanupComponent(entity: Entity) = Unit

    open fun World.initPrefabs(entity: Entity) = Unit
    open fun World.cleanupPrefabs(entity: Entity) = Unit

    /**
     * This function clones the component from the pool and initializes it to contain
     * the same data/properties as the original component. This feature is used to make snapshots on the fly.
     */
    abstract fun clone(): PoolableComponent<T>

    /**
     * The free function needs to be called if the component is not used anymore and should be freed.
     * Normally, this is done in the onRemove function of the component. But SnapshotSerializerSystem will call
     * this function directly to free the component when cleanup old snapshots.
     */
    abstract fun free()

    /**
     * Function that is called when the component is added to an entity.
     */
    override fun World.onAdd(entity: Entity) {
        val gameState = inject<GameStateManager>("GameState")

        // Only run init function on components when the game is running and not when we load snapshots
        if (gameState.gameRunning) {
            initComponent(entity)
        }
        // Call init prefabs always
        initPrefabs(entity)
    }

    /**
     * Function that is called when the component is removed from an entity.
     */
    override fun World.onRemove(entity: Entity) {
        val gameState = inject<GameStateManager>("GameState")

        // Call cleanup prefabs always
        cleanupPrefabs(entity)
        // Do not free the component if the game is not running - i.e. during the snapshot rewind / forward feature
        if (gameState.gameRunning) {
            // Call cleanup function to reset the component when requested by fleks world
            cleanupComponent(entity)
            free()
        }
    }
}
