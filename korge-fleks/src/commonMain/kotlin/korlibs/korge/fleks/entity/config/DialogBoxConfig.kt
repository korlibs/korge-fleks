package korlibs.korge.fleks.entity.config

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
@Serializable
data class DialogBoxConfig(
    override val name: String,

    val duration: Float,

    // Position and size
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val width: Float,
    private val height: Float,

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
    @Serializable(with = HorizontalAlignAsDouble::class) private val horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
    @Serializable(with = VerticalAlignAsDouble::class) private val verticalAlign: VerticalAlign = VerticalAlign.TOP,
) : EntityConfig {

    enum class AvatarPosition { LEFT_TOP, RIGHT_TOP /*, LEFT_BOTTOM, RIGHT_BOTTOM*/ }

    override fun World.entityConfigure(entity: Entity) : Entity {
        val avatarLeftInitialX = x + 10f
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
            it += PositionComponent(x = avatarInitialX, y = y - 24f)
            it += SpriteComponent(name = avatarName)
            it += RgbaComponent().apply {
                tint = this@DialogBoxConfig.tint
                alpha = this@DialogBoxConfig.alpha
            }
            it += RenderLayerTag.FG_DIALOGS
            it += LayerComponent(layerIndex = 102)
        }
        val textField = entity {
            it += PositionComponent(x = this@DialogBoxConfig.x, y = this@DialogBoxConfig.y)
            // TODO replace SpriteComponent by NinePatchComponent (decide if we want a ninePatch property in SpriteComponent or a new component...)
            it += SpriteComponent(name = textFieldName)
            it += RgbaComponent().apply {
                tint = this@DialogBoxConfig.tint
                alpha = this@DialogBoxConfig.alpha
            }
            it += RenderLayerTag.FG_DIALOGS
            it += LayerComponent(layerIndex = 100)
        }
        val text = entity {
            it += PositionComponent(x = this@DialogBoxConfig.x + 7f, y = this@DialogBoxConfig.y + 4f)
            it += TextFieldComponent(
                text = this@DialogBoxConfig.text,
                fontName = this@DialogBoxConfig.textFontName,
                textRangeEnd = this@DialogBoxConfig.textRangeEnd,
                width = this@DialogBoxConfig.width,
                height = this@DialogBoxConfig.height,
                horizontalAlign = this@DialogBoxConfig.horizontalAlign,
                verticalAlign = this@DialogBoxConfig.verticalAlign
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
                            TweenRgba(entity = textField, alpha = 1f, delay = 0.3f, duration = 0.3f, easing = Easing.EASE_OUT_QUAD),
                            // Type write text into the dialog
                            TweenTextField(entity = text, textRangeEnd = this@DialogBoxConfig.text.length, delay = 0.3f + 0.8f, duration = this@DialogBoxConfig.text.length * 0.07f)
                        )
                    ),
                    Wait(duration = this@DialogBoxConfig.duration),
                    ParallelTweens(
                        duration = 1f,
                        tweens = listOf(
                            // Fade out of Dialog with avatar
                            TweenRgba(entity = avatar, alpha = 0f, duration = 0.5f, easing = Easing.EASE_IN_QUAD),
                            TweenPosition(entity = avatar, x = avatarInitialX, duration = 0.5f, easing = Easing.EASE_IN_QUAD),
                            TweenRgba(entity = textField, alpha = 0f, duration = 0.3f, easing = Easing.EASE_IN_QUAD),
                            TweenRgba(entity = text, alpha = 0f, duration = 0.3f, easing = Easing.EASE_IN_QUAD)
                        )
                    ),
                    DeleteEntity(entity = avatar),
                    DeleteEntity(entity = textField),
                    DeleteEntity(entity = text),
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
