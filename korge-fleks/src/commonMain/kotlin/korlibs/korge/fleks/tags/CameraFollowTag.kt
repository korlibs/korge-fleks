package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.*
import kotlinx.serialization.*


/**
 * If this tag is attached to an entity then the camera is following that entity with its viewport.
 */
@Serializable @SerialName("CameraFollow")
data object CameraFollowTag : EntityTag()
