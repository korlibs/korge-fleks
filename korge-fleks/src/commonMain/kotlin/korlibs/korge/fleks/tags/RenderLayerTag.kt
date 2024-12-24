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
    MAIN_SPRITES,
    MAIN_EFFECTS,
    MAIN_FOREGROUND,
    // Foreground layers
    FG_LEVELMAP,
    FG_PARALLAX,
    FG_DIALOGS,
    // Debug shape layers
    DEBUG
}
