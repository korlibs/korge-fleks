package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.image.text.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.tags.*


data class RichTextConfig(
    override val name: String,

    private val text: String,
    private val fontName: String,
    private val layerTag: RenderLayerTag,

    // Position and size of text field
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val width: Float = 0f,  // width and height is used only for alignment of the text
    private val height: Float = 0f,
    private val textRangeEnd: Int = Int.MAX_VALUE,

    private val layerIndex: Int = 1,

    // Color and alpha channel of text graphics
    private val tint: RgbaComponent.Rgb = RgbaComponent.Rgb.WHITE,
    private val alpha: Float = 1f,

    // Text alignment
    private val horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
    private val verticalAlign: VerticalAlign = VerticalAlign.TOP
) : EntityConfig {

    override val configureEntity = fun(world: World, entity: Entity) : Entity = with(world) {
        entity.configure {
            it += PositionComponent(
                x = this@RichTextConfig.x,
                y = this@RichTextConfig.y
            )
            it += TextFieldComponent(
                text = this@RichTextConfig.text,
                fontName = this@RichTextConfig.fontName,
                textRangeEnd = this@RichTextConfig.textRangeEnd,
                width = this@RichTextConfig.width,
                height = this@RichTextConfig.height,
                horizontalAlign = this@RichTextConfig.horizontalAlign,
                verticalAlign = this@RichTextConfig.verticalAlign
            )
            it += LayerComponent(layerIndex = this@RichTextConfig.layerIndex)
            it += RgbaComponent().apply {
                tint = this@RichTextConfig.tint
                alpha = this@RichTextConfig.alpha
            }
            it += layerTag
//            it += RenderLayerTag.DEBUG
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
