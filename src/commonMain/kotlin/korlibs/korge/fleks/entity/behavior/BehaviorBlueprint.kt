package korlibs.korge.fleks.entity.behavior

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World


/**
 * This interface maps the string [name] to a specific behavior object configuration.
 * These objects are created at build time and can be configured
 * through additional properties in the derived class.
 *
 * Hint:
 * Deriving the configuration for a behavior object from this interface keeps the configuration details
 * (config properties) together with the creation process of a complex behavior controller object.
 * The [name] is used as key in [BehaviorStorage] and must match the value stored in the
 * [Behavior][korlibs.korge.fleks.components.Behavior] component's
 * [name][korlibs.korge.fleks.components.Behavior.name] field.
 */
interface BehaviorBlueprint {
    val name: String
    fun World.update(entity: Entity, deltaTime: Float)
}

/**
 * A simple empty Behavior object. This can be used as a placeholder or a default behavior object.
 */
class EmptyBehavior : BehaviorBlueprint {
    override val name = "emptyBehavior"
    override fun World.update(entity: Entity, deltaTime: Float) {}
}
