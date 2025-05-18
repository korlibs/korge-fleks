package korlibs.korge.fleks.tags

import com.github.quillraven.fleks.*
import kotlinx.serialization.*


/**
 * The [RenderLayerTag] component is used to specify which renderer is drawing the specific entity.
 * With that also layering of entity graphics is achieved.
 */
@Serializable @SerialName("RenderLayer")
enum class RenderLayerTag : EntityTags by entityTagOf() {
    // Background layers
    BG_LEVELMAP,
    BG_PARALLAX,
    // Main layers
    MAIN_LEVELMAP,
    MAIN_OBJECT_LAYER,
    MAIN_EFFECTS,
    MAIN_OBJECT_LAYER_FG,
    // Foreground layers
    FG_LEVELMAP,
    FG_PARALLAX,
    FG_OBJECT_DIALOGS,
    // Debug shape layers
    DEBUG
}
