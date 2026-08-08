package korlibs.korge.fleks.entity.behavior


/**
 * This storage contains [BehaviorBlueprint] objects for creating character controllers which define the AI behavior
 * of game objects.
 */
object BehaviorStorage {

    // Internal storage for behavior blueprint objects
    private val behaviorBlueprintBlueprints: MutableMap<String, BehaviorBlueprint> = mutableMapOf()

    private val emptyBehavior = EmptyBehavior()

    /**
     * Adds a [BehaviorBlueprint] to the internal storage.
     * If another [BehaviorBlueprint] with the same [name][BehaviorBlueprint.name] is already
     * registered it will NOT be replaced, so that a base configuration can be shared across multiple entities.
     */
    fun register(blueprint: BehaviorBlueprint) {
        if (!behaviorBlueprintBlueprints.containsKey(blueprint.name)) {
            behaviorBlueprintBlueprints[blueprint.name] = blueprint
        } else {
            println("WARNING: BehaviorTreeBlueprint with name '${blueprint.name}' already registered in BehaviorTreeStorage! New blueprint will NOT be registered!")
        }
    }

    /**
     * Returns `true` if a [BehaviorBlueprint] with the given [name] is already registered.
     */
    fun contains(name: String): Boolean = behaviorBlueprintBlueprints.containsKey(name)

    /**
     * Returns the root [BTNode] for the behavior tree identified by [name].
     *
     * The [name] should match the value stored in the
     * [BehaviorTree][korlibs.korge.fleks.components.Behavior] component's
     * [name][korlibs.korge.fleks.components.Behavior.name] field.
     *
     * @return the [BehaviorBlueprint] created by [BehaviorBlueprint.name], or `emtpyBehavior` if
     *         no blueprint with that name was registered.
     */
    operator fun get(name: String): BehaviorBlueprint {
        val blueprint = behaviorBlueprintBlueprints[name]
        return if (blueprint != null) {
            blueprint
        } else {
            println("ERROR: BehaviorTreeBlueprint with name '$name' not registered in BehaviorTreeStorage!")
            emptyBehavior
        }
    }
}
