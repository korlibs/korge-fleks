package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.*
import kotlinx.serialization.*


/**
 * The [ScreenCoordinatesTag] component when attached to an entity indicates that the entity's position
 * is in screen coordinates.
 * If it is not added (default) than the entity's position is in world coordinates and the
 * entity's position will be translated from world to screen coordinates in the render systems.
 */
@Serializable @SerialName("ScreenCoordinates")
data object ScreenCoordinatesTag : EntityTag()
