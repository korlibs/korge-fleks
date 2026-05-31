package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.image.text.HorizontalAlign
import korlibs.image.text.VerticalAlign
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component contains [text] which can be drawn with [fontName] with the DialogRenderView.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanup] function and initialize them in the [init] function.
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
) : PoolableComponent<TextField>() {
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
        fun staticTextFieldComponent(config: TextField.() -> Unit): TextField =
            TextField().apply(config)

        // Use this function to get a new instance of a component from the pool and add it to an entity
        fun textFieldComponent(config: TextField.() -> Unit): TextField =
            pool.alloc().apply(config)

        private val pool = Pool(AppConfig.POOL_PREALLOCATE, "TextField") { TextField() }
    }

    // Clone a new instance of the component from the pool
    override fun clone(): TextField = textFieldComponent { init(from = this@TextField) }

    // Initialize the component automatically when it is added to an entity
    override fun World.initComponent(entity: Entity) {
    }

    // Cleanup/Reset the component automatically when it is removed from an entity (component will be returned to the pool eventually)
    override fun World.cleanupComponent(entity: Entity) {
        cleanup()
    }

    // Initialize an external prefab when the component is added to an entity
    override fun World.initPrefabs(entity: Entity) {
    }

    // Cleanup/Reset an external prefab when the component is removed from an entity
    override fun World.cleanupPrefabs(entity: Entity) {
    }

    // Free the component and return it to the pool - this is called directly by the SnapshotSerializerSystem
    override fun free() {
        cleanup()
        pool.free(this)
    }
}
