package korlibs.korge.fleks.entity.behavior

/**
 * This interface maps the string [name] to a specific behavior tree configuration.
 * The behavior tree root node [btree] is created at build time and can be configured
 * through additional properties in the derived class.
 *
 * Hint:
 * Deriving the configuration for a behavior tree from this interface keeps the configuration details
 * (config properties) together with the creation process of a complex behavior tree which can consist
 * of multiple nested [BTNode]s. The [name] is used as key in [BehaviorTreeStorage] and must match the
 * value stored in the [BehaviorTree][korlibs.korge.fleks.components.BehaviorTree] component's
 * [characterConfig][korlibs.korge.fleks.components.BehaviorTree.characterConfig] field.
 */
interface BehaviorTreeBlueprint {
    val name: String
    val btree: BTNode
}
