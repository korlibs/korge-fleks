package korlibs.korge.fleks.entity.config

import AppConfig
import com.github.quillraven.fleks.*
import korlibs.image.text.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenSequenceComponent.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.*

/**
 * Entity config for a dialog box which appears on the dialog layer in front of any game play.
 * Dialog Box is rendered on indexLayer 100 - 102 in foreground on [FG_DIALOGS][RenderLayerTag.FG_DIALOGS] layer.
 */
@Serializable @SerialName("DialogBoxConfig")
data class DialogBoxConfig(
    override val name: String,

    private val duration: Float,

    // Position and size
    @Serializable(with = HAlignAsString::class) private val positionHAlign: HorizontalAlign,
    @Serializable(with = VAlignAsString::class) private val positionVAlign: VerticalAlign,
    private val width: Float,
    private val numberOfLines: Int,  // TODO get this from placed text

    // Color and alpha channel of text and graphics
    private val tint: RgbaComponent.Rgb = RgbaComponent.Rgb.WHITE,
    private val alpha: Float = 0f,

    // Avatar
    private val avatarMoveX: Float = 10f,
    private val avatarName: String,
    private val avatarPosition: AvatarPosition,

    // Text field
    private val textFieldName: String,

    // Text
    private val text: String,
    private val textFontName: String,
    private val textRangeEnd: Int = 0,  // initial value for drawing the text into the dialog
    @Serializable(with = HAlignAsString::class) private val textHAlign: HorizontalAlign = HorizontalAlign.LEFT,
    @Serializable(with = VAlignAsString::class) private val textVAlign: VerticalAlign = VerticalAlign.TOP,
) : EntityConfig {

    enum class AvatarPosition { LEFT_TOP, RIGHT_TOP /*, LEFT_BOTTOM, RIGHT_BOTTOM*/ }

    private val textSize = 13f  // TODO get this from used font (lineHeight)
    private val height: Float = textSize * numberOfLines + 2

    private fun hAlign(h: HorizontalAlign, width: Float) : Float {
        if (h.ratio * AppConfig.TARGET_VIRTUAL_WIDTH < 10) return 10f
        if (h.ratio * AppConfig.TARGET_VIRTUAL_WIDTH > AppConfig.TARGET_VIRTUAL_WIDTH - 10) return AppConfig.TARGET_VIRTUAL_WIDTH - width - 10
        return (h.ratio * AppConfig.TARGET_VIRTUAL_WIDTH).toFloat()
    }

    private fun vAlign(v: VerticalAlign, height: Float) : Float {
        if (v.ratio * AppConfig.TARGET_VIRTUAL_HEIGHT < 34) return 34f
        if (v.ratio * AppConfig.TARGET_VIRTUAL_HEIGHT > AppConfig.TARGET_VIRTUAL_HEIGHT - 10) return AppConfig.TARGET_VIRTUAL_HEIGHT - height - 10
        return (v.ratio * AppConfig.TARGET_VIRTUAL_HEIGHT).toFloat()
    }

    override fun World.entityConfigure(entity: Entity) : Entity {

        val textBoxWidth = width + 14  // 190 <- 176
        val textBoxHeight = height + 8  // 49 <- 41
        val textBoxX = hAlign(positionHAlign, textBoxWidth)
        val textBoxY = vAlign(positionVAlign, textBoxHeight)
        val textFieldX = textBoxX + 7
        val textFieldY = textBoxY + 4
        val textFieldWidth = width
        val textFieldHeight = height


        val avatarLeftInitialX = textBoxX + 10f
        val avatarRightInitialX = AppConfig.TARGET_VIRTUAL_WIDTH - 20f - 38f
        val avatarInitialX = when (avatarPosition) {
            AvatarPosition.LEFT_TOP -> avatarLeftInitialX - avatarMoveX
            AvatarPosition.RIGHT_TOP -> avatarRightInitialX + avatarMoveX
        }
        val avatarTweenX = when (avatarPosition) {
            AvatarPosition.LEFT_TOP -> avatarLeftInitialX
            AvatarPosition.RIGHT_TOP -> avatarRightInitialX
        }
        // Avatar image entity
        val avatar = entity {
            it += PositionComponent(x = avatarInitialX, y = textBoxY - 24f)
            it += SpriteComponent(name = avatarName)
            it += RgbaComponent().apply {
                tint = this@DialogBoxConfig.tint
                alpha = this@DialogBoxConfig.alpha
            }
            it += RenderLayerTag.FG_DIALOGS
            it += LayerComponent(layerIndex = 102)
//            it += RenderLayerTag.DEBUG
        }
        val textBox = entity {
            it += PositionComponent(x = textBoxX, y = textBoxY)
            it += NinePatchComponent(
                name = textFieldName,
                width = textBoxWidth,
                height = textBoxHeight
            )
            it += RgbaComponent().apply {
                tint = this@DialogBoxConfig.tint
                alpha = this@DialogBoxConfig.alpha
            }
            it += RenderLayerTag.FG_DIALOGS
            it += LayerComponent(layerIndex = 100)
//            it += RenderLayerTag.DEBUG
        }
        val textField = entity {
            it += PositionComponent(x = textFieldX, y = textFieldY)
            it += TextFieldComponent(
                text = text,
                fontName = textFontName,
                textRangeEnd = textRangeEnd,
                width = textFieldWidth,
                height = textFieldHeight,
                horizontalAlign = textHAlign,
                verticalAlign = textVAlign
            )
            it += LayerComponent(layerIndex = 101)
            it += RgbaComponent().apply {
                tint = this@DialogBoxConfig.tint
                alpha = 1f
            }
            it += RenderLayerTag.FG_DIALOGS
//            it += RenderLayerTag.DEBUG
        }
        entity.configure {
            it += TweenSequenceComponent(
                tweens = listOf(
                    ParallelTweens(
                        tweens = listOf(
                            // Fade in of Dialog with avatar
                            TweenRgba(entity = avatar, alpha = 1f, duration = 0.5f, easing = Easing.EASE_OUT_QUAD),
                            TweenPosition(entity = avatar, x = avatarTweenX, duration = 0.5f, easing = Easing.EASE_OUT_QUAD),
                            TweenRgba(entity = textBox, alpha = 1f, delay = 0.3f, duration = 0.3f, easing = Easing.EASE_OUT_QUAD),
                            // Type write text into the dialog
                            TweenTextField(entity = textField, textRangeEnd = text.length, delay = 0.3f + 0.8f, duration = text.length * 0.07f)
                        )
                    ),
                    Wait(duration = duration),
                    ParallelTweens(
                        duration = 1f,
                        tweens = listOf(
                            // Fade out of Dialog with avatar
                            TweenRgba(entity = avatar, alpha = 0f, duration = 0.5f, easing = Easing.EASE_IN_QUAD),
                            TweenPosition(entity = avatar, x = avatarInitialX, duration = 0.5f, easing = Easing.EASE_IN_QUAD),
                            TweenRgba(entity = textBox, alpha = 0f, duration = 0.3f, easing = Easing.EASE_IN_QUAD),
                            TweenRgba(entity = textField, alpha = 0f, duration = 0.3f, easing = Easing.EASE_IN_QUAD)
                        )
                    ),
                    DeleteEntity(entity = avatar),
                    DeleteEntity(entity = textBox),
                    DeleteEntity(entity = textField),
                    DeleteEntity(entity = it)
                )
            )
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
