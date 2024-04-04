package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*


/**
 * The [RenderLayerTag] component is used to specify which renderer is drawing the specific entity.
 * With that also layering of entity graphics is achieved.
 */

enum class RenderLayerTag : EntityTags by entityTagOf() {
    BG_PARALLAX,
    BG_LEVELMAP,
    MAIN_1,
    MAIN_2,
    MAIN_3,
    MAIN_4,
    MAIN_5,
    FG_PARALLAX,
    FG_LEVELMAP,
    DEBUG
}
