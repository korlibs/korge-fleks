package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.text.*
import korlibs.korge.fleks.components.Layer.Companion.layerComponent
import korlibs.korge.fleks.components.NinePatch.Companion.ninePatchComponent
import korlibs.korge.fleks.components.Position.Companion.positionComponent
import korlibs.korge.fleks.components.Rgba.Companion.rgbaComponent
import korlibs.korge.fleks.components.Sprite.Companion.spriteComponent
import korlibs.korge.fleks.components.TextField.Companion.textFieldComponent
import korlibs.korge.fleks.components.TweenSequence.Companion.tweenSequenceComponent
import korlibs.korge.fleks.components.data.tweenSequence.DeleteEntity.Companion.deleteEntity
import korlibs.korge.fleks.components.data.tweenSequence.ParallelTweens.Companion.parallelTweens
import korlibs.korge.fleks.components.data.tweenSequence.TweenPosition.Companion.tweenPosition
import korlibs.korge.fleks.components.data.tweenSequence.TweenRgba.Companion.tweenRgba
import korlibs.korge.fleks.components.data.tweenSequence.TweenTextField.Companion.tweenTextField
import korlibs.korge.fleks.components.data.tweenSequence.Wait.Companion.wait
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.tags.*
import korlibs.korge.fleks.utils.*
import korlibs.math.interpolation.*
import kotlinx.serialization.*

/**
 * Entity config for a dialog box which appears on the dialog layer in front of any game play.
 * Dialog Box is rendered on indexLayer 100 - 102 in foreground on [FG_DIALOGS][RenderLayerTag.FG_OBJECT_DIALOGS] layer.
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
    @Serializable(with = RGBAAsInt::class) private val tint: RGBA = Colors.WHITE,
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
    private val textWritingFactor: Float = 0.025f,  // factor to calculate the time for writing the text into the dialog box
    @Serializable(with = HAlignAsString::class) private val textHAlign: HorizontalAlign = HorizontalAlign.LEFT,
    @Serializable(with = VAlignAsString::class) private val textVAlign: VerticalAlign = VerticalAlign.TOP,
) : EntityConfig {

    enum class AvatarPosition { LEFT_TOP, RIGHT_TOP /*, LEFT_BOTTOM, RIGHT_BOTTOM*/ }

    private val textSize = 13f  // TODO get this from used font (lineHeight)
    private val height: Float = textSize * numberOfLines + 10

    private fun hAlign(h: HorizontalAlign, width: Float, viewPortWidth: Float) : Float {
        if (h.ratio * viewPortWidth < 10) return 10f
        if (h.ratio * viewPortWidth > viewPortWidth - 10) return viewPortWidth - width - 10
        return (h.ratio * viewPortWidth).toFloat()
    }

    private fun vAlign(v: VerticalAlign, height: Float, viewPortHeight: Float) : Float {
        if (v.ratio * viewPortHeight < 34) return 34f
        if (v.ratio * viewPortHeight > viewPortHeight - 10) return viewPortHeight - height - 10
        return (v.ratio * viewPortHeight).toFloat()
    }

    override fun World.entityConfigure(entity: Entity) : Entity {

        val viewPortWidth = AppConfig.VIEW_PORT_WIDTH.toFloat()
        val viewPortHeight = AppConfig.VIEW_PORT_HEIGHT.toFloat()

        val dialogBoxWidth = width
        val dialogBoxHeight = height
        // Calculate the position of the dialog box depending on the horizontal and vertical alignment
        val textBoxX = hAlign(positionHAlign, dialogBoxWidth, viewPortWidth)
        val textBoxY = vAlign(positionVAlign, dialogBoxHeight, viewPortHeight)

        val textFieldX = textBoxX + 5
        val textFieldY = textBoxY + 4
        val textFieldWidth = width - 10
        val textFieldHeight = height - 8


        val avatarLeftInitialX = textBoxX + 10f
        val avatarRightInitialX = viewPortWidth - 20f - 38f
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
            it += ScreenCoordinatesTag
            it += positionComponent {
                x = avatarInitialX
                y = textBoxY - 24f
            }
            it += spriteComponent { name = avatarName }
            it += rgbaComponent {
                rgba = tint
                alpha = this@DialogBoxConfig.alpha
            }
            it += RenderLayerTag.FG_OBJECT_DIALOGS
            it += layerComponent { index = 102 }
//            it += RenderLayerTag.DEBUG
        }
        val dialogBox = entity {
            it += ScreenCoordinatesTag
            it += positionComponent {
                x = textBoxX
                y = textBoxY
            }
            it += ninePatchComponent {
                name = textFieldName
                width = dialogBoxWidth
                height = dialogBoxHeight
            }
            it += rgbaComponent {
                rgba = tint
                alpha = this@DialogBoxConfig.alpha
            }
            it += RenderLayerTag.FG_OBJECT_DIALOGS
            it += layerComponent { index = 100 }
            it += RenderLayerTag.DEBUG
            it += DebugInfoTag.NINE_PATCH_BOUNDS
        }
        val textField = entity {
            it += ScreenCoordinatesTag
            it += positionComponent {
                x = textFieldX
                y = textFieldY
            }
            it += textFieldComponent {
                text = this@DialogBoxConfig.text
                fontName = textFontName
                textRangeEnd = this@DialogBoxConfig.textRangeEnd
                width = textFieldWidth
                height = textFieldHeight
                horizontalAlign = textHAlign
                verticalAlign = textVAlign
            }
            it += layerComponent { index = 101 }
            it += rgbaComponent {
                rgba = tint
                alpha = 1f
            }
            it += RenderLayerTag.FG_OBJECT_DIALOGS
            it += RenderLayerTag.DEBUG
            it += DebugInfoTag.TEXT_FIELD_BOUNDS
        }
        entity.configure {
            it += tweenSequenceComponent {
                parallelTweens {
                    // Fade in of Dialog with avatar
                    tweenRgba { target = avatar; alpha = 1f; duration = 0.5f; easing = Easing.EASE_OUT_QUAD }
                    tweenPosition { target = avatar; x = avatarTweenX; duration = 0.5f; easing = Easing.EASE_OUT_QUAD }
                    tweenRgba { target = dialogBox; alpha = 1f; delay = 0.3f; duration = 0.3f; easing = Easing.EASE_OUT_QUAD }
                    // Type write text into the dialog
                    tweenTextField { target = textField; textRangeEnd = this@DialogBoxConfig.text.length; delay = 0.3f + 0.8f; duration = this@DialogBoxConfig.text.length * textWritingFactor }
                }
                wait { duration = this@DialogBoxConfig.duration }
                parallelTweens {
                    duration = 1f
                    // Fade out of Dialog with avatar
                    tweenRgba { target = avatar; alpha = 0f; duration = 0.5f; easing = Easing.EASE_IN_QUAD }
                    tweenPosition { target = avatar; x = avatarInitialX; duration = 0.5f; easing = Easing.EASE_IN_QUAD }
                    tweenRgba { target = dialogBox; alpha = 0f; duration = 0.3f; easing = Easing.EASE_IN_QUAD }
                    tweenRgba { target = textField; alpha = 0f; duration = 0.3f; easing = Easing.EASE_IN_QUAD }
                }
                parallelTweens {
                    deleteEntity { target = avatar }
                    deleteEntity { target = dialogBox }
                    deleteEntity { target = textField }
                    deleteEntity { target = it }
                }
            }
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
