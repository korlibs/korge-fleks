package korlibs.korge.fleks.tags


import com.github.quillraven.fleks.*
import kotlinx.serialization.*

/**
 * The [ViewTag] component is used to specify that the entity contains a Korge View
 * which needs to be put into a "layer" container.
 */
@Serializable @SerialName("View")
data object ViewTag : EntityTag()
