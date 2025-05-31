package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.EntityTags
import com.github.quillraven.fleks.entityTagOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This tag is used to mark parts of an entity visible as bounding box or position cross on the debug layer.
 * The data from this tag will be processed by the [DebugRenderSystem] in Korge-fleks.
 */
@Serializable @SerialName("DebugInfo")
enum class DebugInfoTag : EntityTags by entityTagOf() {
    POSITION,
    GRID_POSITION,

    SPRITE_BOUNDS,
    SPRITE_TEXTURE_BOUNDS,
    SPRITE_COLLISION_BOUNDS,

    TEXT_FIELD_BOUNDS,
    NINE_PATCH_BOUNDS,

    LEVEL_MAP_COLLISION_BOUNDS,
}
