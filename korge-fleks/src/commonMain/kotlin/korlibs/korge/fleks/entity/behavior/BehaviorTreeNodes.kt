package korlibs.korge.fleks.entity.behavior

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World


// ---------- Lightweight Behavior Tree primitives ---------------------------------------------------------------------

enum class BTStatus { Success, Failure, Running }

interface BTNode {
    fun World.tick(entity: Entity, deltaTime: Float): BTStatus
}

/**
 * A simple leaf node that always returns success. This can be used as a placeholder or a default node in the behavior tree.
 */
class EmptyTree : BTNode {
    override fun World.tick(entity: Entity, deltaTime: Float): BTStatus = BTStatus.Success
}

/**
 * First Composite Node: A selector node (symbol: [?]) that ticks its children in order and returns success on the
 * first child that succeeds, failure if all children failed, and running if any child is still running.
 */
class Selector(private val children: List<BTNode>) : BTNode {
    override fun World.tick(entity: Entity, deltaTime: Float): BTStatus {
        children.forEach { child ->
            when (child.run { tick(entity, deltaTime) }) {
                BTStatus.Success -> return BTStatus.Success
                BTStatus.Running -> return BTStatus.Running
                BTStatus.Failure ->  {}
            }
        }
        return BTStatus.Failure
    }
}

/**
 * Second Composite Node: A sequence node (symbol: [->]) that ticks its children in order and returns failure on the
 * first child that fails, success if all children succeed, and running if any child is still running.
 */
class Sequence(private val children: List<BTNode>) : BTNode {
    override fun World.tick(entity: Entity, deltaTime: Float): BTStatus {
        children.forEach { child ->
            when (child.run { tick(entity, deltaTime) }) {
                BTStatus.Success -> {}
                BTStatus.Failure -> return BTStatus.Failure
                BTStatus.Running -> return BTStatus.Running
            }
        }
        return BTStatus.Success
    }
}

/**
 * Decorator Nodes: A node that wraps a single child and modifies its behavior. For example, an inverter node
 * (symbol: [!]) that inverts the success/failure status of its child, or a repeater node (symbol: [*]) that repeats
 * its child a certain number of times or until a certain condition is met.
 *
 * TODO: Implement when needed
 */

/**
 * First Leaf Nodes: Action nodes that perform an action and return success/failure/running based on the outcome, and
 * Condition nodes that evaluate a condition and return success if true, failure if false, and possibly running if
 * the condition is still being evaluated (e.g., waiting for a timer or an event). These nodes will directly
 * manipulate the blackboard to read/write values needed for the behavior tree logic.
 */
class ActionNode(private val action: World.(Entity, Float) -> BTStatus) : BTNode {
    override fun World.tick(entity: Entity, deltaTime: Float): BTStatus = action(entity, deltaTime)
}

/**
 * Second Leaf Node: A condition node that evaluates a condition and returns success if true, failure if false.
 * For simplicity, we assume conditions are evaluated instantly and do not return Running.
 * The condition is a function that takes the blackboard as input and returns a boolean.
 */
class ConditionNode(private val cond: World.(Entity, Float) -> BTStatus) : BTNode {
    override fun World.tick(entity: Entity, deltaTime: Float): BTStatus = cond(entity, deltaTime)
}
