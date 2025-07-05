package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.EntityTag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This tag is used to mark an entity as intentionally empty.
 * It can be used to indicate that the entity is not meant to have any components or data associated with it.
 * This can be useful for placeholder entities or for entities that are meant to be empty by design.
 */
@Serializable @SerialName("IntentionallyEmpty")
data object IntentionallyEmpty : EntityTag()
