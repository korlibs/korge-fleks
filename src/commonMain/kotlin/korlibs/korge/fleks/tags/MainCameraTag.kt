package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.*
import kotlinx.serialization.*

/**
 * The [MainCameraTag] component is used to specify which camera entity is used for rendering the game world.
 * Make sure to have only one camera entity in the game world with has [MainCameraTag] component attached. Otherwise,
 * the first camera entity found by the family search will be used for rendering.
 */
@Serializable @SerialName("MainCamera")
data object MainCameraTag : EntityTag()
