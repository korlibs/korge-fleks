package korlibs.korge.fleks.entity.behavior


/**
 * This storage contains [BehaviorTreeBlueprint]'s for creating [BTNode] trees which define the AI behavior
 * of game objects.
 *
 * New [BehaviorTreeBlueprint]'s need to be added via [register] before they can be retrieved.
 * Systems (e.g. a BehaviorTreeSystem) call [get] with the name stored in the
 * [BehaviorTree][korlibs.korge.fleks.components.BehaviorTree] component's
 * [characterConfig][korlibs.korge.fleks.components.BehaviorTree.characterConfig] field to obtain
 * the root [BTNode] for ticking.
 */
object BehaviorTreeStorage {

    // Internal storage for behavior tree blueprint objects
    private val behaviorTreeBlueprints: MutableMap<String, BehaviorTreeBlueprint> = mutableMapOf()

    private val emptyTreeNode = EmptyTreeNode()

    /**
     * Adds a [BehaviorTreeBlueprint] to the internal storage.
     * If another [BehaviorTreeBlueprint] with the same [name][BehaviorTreeBlueprint.name] is already
     * registered it will NOT be replaced, so that a base configuration can be shared across multiple entities.
     */
    fun register(blueprint: BehaviorTreeBlueprint) {
        if (!behaviorTreeBlueprints.containsKey(blueprint.name)) {
            behaviorTreeBlueprints[blueprint.name] = blueprint
        } else {
            println("WARNING: BehaviorTreeBlueprint with name '${blueprint.name}' already registered in BehaviorTreeStorage! New blueprint will NOT be registered!")
        }
    }

    /**
     * Returns `true` if a [BehaviorTreeBlueprint] with the given [name] is already registered.
     */
    fun contains(name: String): Boolean = behaviorTreeBlueprints.containsKey(name)

    /**
     * Returns the root [BTNode] for the behavior tree identified by [name].
     *
     * The [name] should match the value stored in the
     * [BehaviorTree][korlibs.korge.fleks.components.BehaviorTree] component's
     * [characterConfig][korlibs.korge.fleks.components.BehaviorTree.characterConfig] field.
     *
     * @return the root [BTNode] created by [BehaviorTreeBlueprint.createBehaviorTree], or `emtpyTree` if
     *         no blueprint with that name was registered.
     */
    fun get(name: String): BTNode {
        val blueprint = behaviorTreeBlueprints[name]
        return if (blueprint != null) {
            blueprint.btree
        } else {
            println("ERROR: BehaviorTreeBlueprint with name '$name' not registered in BehaviorTreeStorage!")
            emptyTreeNode
        }
    }
}