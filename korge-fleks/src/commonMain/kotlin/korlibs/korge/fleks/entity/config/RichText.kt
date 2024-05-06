package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.*
import korlibs.image.text.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.entity.EntityFactory.EntityConfig
import korlibs.korge.fleks.tags.*


data class RichText(
    override val name: String,

    private val text: String,
    private val fontName: String,
    private val layerTag: RenderLayerTag,

    // Position and size of text field
    private val x: Float = 0f,
    private val y: Float = 0f,
    private val width: Float,
    private val height: Float,

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
                x = this@RichText.x,
                y = this@RichText.y
            )
            it += TextComponent(
                text = this@RichText.text,
                fontName = this@RichText.fontName,
                width = this@RichText.width,
                height = this@RichText.height,
                horizontalAlign = this@RichText.horizontalAlign,
                verticalAlign = this@RichText.verticalAlign
            )
            it += LayerComponent(layerIndex = this@RichText.layerIndex)
            it += RgbaComponent().apply {
                tint = this@RichText.tint
                alpha = this@RichText.alpha
            }
            it += layerTag
        }
        return entity
    }

    init {
        EntityFactory.register(this)
    }
}
