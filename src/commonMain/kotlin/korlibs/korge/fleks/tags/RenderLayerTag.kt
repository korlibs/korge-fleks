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
    BG_OBJECT_LAYER,
    BG_PARALLAX_LAYER,
    // Main layers
    MAIN_LEVELMAP,
    MAIN_OBJECT_LAYER,
    MAIN_EFFECT_LAYER,
    MAIN_FG_OBJECT_LAYER,
    // Foreground layers
    FG_LEVELMAP,
    FG_PARALLAX_LAYER,
    FG_DIALOGS_LAYER,
    // Debug shape layers
    DEBUG
}
