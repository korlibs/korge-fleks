package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.text.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component contains [text] which can be drawn with [fontName] with the DialogRenderView.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("TextField")
class TextField private constructor(
    var text: String = "",
    var fontName: String = "",

    var textRangeStart: Int = 0,
    var textRangeEnd: Int = Int.MAX_VALUE,

    // size of text bounds
    var width: Float = 0f,
    var height: Float = 0f,
    var wordWrap: Boolean = true,
    @Serializable(with = HorizontalAlignAsDouble::class) var horizontalAlign: HorizontalAlign = HorizontalAlign.LEFT,
    @Serializable(with = VerticalAlignAsDouble::class) var verticalAlign: VerticalAlign = VerticalAlign.TOP
) : PoolableComponents<TextField>() {
    // Init an existing component data instance with data from another component
    // This is used for component instances when they are part (val property) of another component
    fun init(from: TextField) {
        text = from.text
        fontName = from.fontName
        textRangeStart = from.textRangeStart
        textRangeEnd = from.textRangeEnd
        width = from.width
        height = from.height
        wordWrap = from.wordWrap
        // Perform deep copy of Alignment enums
        horizontalAlign = from.horizontalAlign.clone()
        verticalAlign = from.verticalAlign.clone()
    }

    // Cleanup the component data instance manually
    // This is used for component instances when they are part (val property) of another component
    fun cleanup() {
        text = ""
        fontName = ""
        textRangeStart = 0
        textRangeEnd = Int.MAX_VALUE
        width = 0f
        height = 0f
        wordWrap = true
        horizontalAlign = HorizontalAlign.LEFT
        verticalAlign = VerticalAlign.TOP
    }

    override fun type() = TextFieldComponent

    companion object {
        val TextFieldComponent = componentTypeOf<TextField>()

        // Use this function to create a new instance of component data as val inside another component
        fun staticTextFieldComponent(config: TextField.() -> Unit ): TextField =
            TextField().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun World.TextFieldComponent(config: TextField.() -> Unit ): TextField =
        getPoolable(TextFieldComponent).apply(config)

        // Call this function in the fleks world configuration to create the component pool
        fun InjectableConfiguration.addTextFieldComponentPool(preAllocate: Int = 0) {
            addPool(TextFieldComponent, preAllocate) { TextField() }
        }
    }

    // Clone a new instance of the component from the pool
    override fun World.clone(): TextField =
        getPoolable(TextFieldComponent).apply { init(from = this@TextField ) }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }
}
