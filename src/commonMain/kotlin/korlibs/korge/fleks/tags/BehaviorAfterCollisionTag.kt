package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * This tag component is used to process a behavior of an entity the second time after collision.
 * This can be used e.g. to react to a collision trigger event still within the same frame.
 */
@Serializable @SerialName("BehaviorAfterCollisionTag")
data object BehaviorAfterCollisionTag : EntityTag()
