package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.image.format.*
import korlibs.image.text.*
import korlibs.io.lang.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.components.TweenSequenceComponent.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.entity.EntityFactory
import korlibs.korge.fleks.tags.*
import korlibs.math.interpolation.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*
import kotlin.jvm.JvmInline


/**
 * All data classes (not deriving from Fleks Component<...>) which are used within components need to be serializable by
 * deriving from this interface.
 */
interface CloneableData<out T> {
    fun clone(): T
}

/**
 * All components which shall be recorded (serialized) in SnapshotSerializerSystem needs to be derived from
 * [CloneableComponent].
 * The clone function needs to be implemented to perform a deep copy of all properties of the component.
 */
abstract class CloneableComponent<T> : Component<T> {
    abstract fun clone(): Component<T>
}

/**
 * Class for serializing identifier objects for entity configs and functions in components.
 *
 * These identifiers are used to access a specific entity configuration from the [AssetStore].
 * They are also used to access a specific lambda function through the [EntityFactory] object.
 * It wraps a string value. Using this [Identifier] object everywhere in the code is more error-prone than using a plain string.
 * Since strings can have typos which are not realized at compile time.
 */
@JvmInline @Serializable
value class Identifier(val name: String)

/**
 * The JsonSerializer is used to store the world snapshot to persistent storage.
 * All Korge-fleks internal components are already registered as polymorphic subclass
 * in the internal module.
 *
 * For all Components and tags which are defined outside Korge-fleks an external
 * module needs to be set up and registered. This can be done like below in the
 * configuration of the fleks world:
 *
 * configureWorld {
 *     systems {
 *         SnapshotSerializerSystem.run {
 *             setup(
 *                 module = SerializersModule {
 *                     // Register additional own data classes here
 *                     polymorphic(SerializeBase::class) {
 *                         subclass(MyData::class)
 *                     }
 *                     // Register additional own component classes here
 *                     polymorphic(Component::class) {
 *                         subclass(MyComponent::class)
 *                     }
 *                     // Register additional own tags (components without properties) here
 *                     polymorphic(UniqueId::class) {
 *                         subclass(MyTag::class)
 *                     }
 *                 }
 *             )
 *         }
 *     }
 * }
 *
 */
class SnapshotSerializer {

    private val modulesMap = mutableMapOf<String, SerializersModule>()
    private lateinit var json: Json
    private var dirty = true

    fun register(name: String, module: SerializersModule) {
        modulesMap[name] = module
        dirty = true
    }

    fun unregister(name: String) {
        modulesMap.remove(name)
    }

    /**
     * Get the Json serializer with all registered modules for serializing and deserializing entities of the fleks world.
     */
    fun json(pretty: Boolean = false) : Json {
        if (dirty) {
            var modules = internalModule
            modulesMap.values.forEach { module ->
                modules = modules.plus(module)
            }

            json = Json {
                prettyPrint = pretty
                serializersModule = modules
                allowStructuredMapKeys = true // to support entity id + version as a key in a map data structure
            }
            dirty = false
        }
        return json
    }

    /**
     * This polymorphic module config for kotlinx serialization lists all Korge-fleks
     * internal components as subclasses.
     */
    private val internalModule = SerializersModule {
        // Register data classes
        polymorphic(CloneableData::class) {
            subclass(ParallaxComponent.Layer::class)
            subclass(ParallaxComponent.Plane::class)
            subclass(RgbaComponent.Rgb::class)
            subclass(OffsetByFrameIndexComponent.Point::class)
            subclass(SpriteLayersComponent.LayerProperties::class)
            subclass(LayeredSpriteComponent.Layer::class)
        }

        // Register component classes
        polymorphic(Component::class) {
            subclass(EntityLinkComponent::class)
            subclass(InfoComponent::class)
            subclass(TouchInputComponent::class)
            subclass(LayerComponent::class)
            subclass(LayeredSpriteComponent::class)
            subclass(LayoutComponent::class)
            subclass(LdtkLevelMapComponent::class)
            subclass(LifeCycleComponent::class)
            subclass(MotionComponent::class)
            subclass(NoisyMoveComponent::class)
            subclass(OffsetByFrameIndexComponent::class)
            subclass(ParallaxComponent::class)
            subclass(PositionComponent::class)
            subclass(RgbaComponent::class)
            subclass(RigidbodyComponent::class)
            subclass(SizeComponent::class)
            subclass(SoundComponent::class)
            subclass(SpawnerComponent::class)
            subclass(SpriteLayersComponent::class)
            subclass(SpriteComponent::class)
            subclass(SubEntitiesComponent::class)
            subclass(SwitchLayerVisibilityComponent::class)
            subclass(TextFieldComponent::class)
            subclass(TiledLevelMapComponent::class)
            subclass(TweenPropertyComponent::class)
            subclass(TweenSequenceComponent::class)
        }
        // Register tags (components without properties)
        polymorphic(UniqueId::class) {
            subclass(RenderLayerTag::class, PolymorphicEnumSerializer( RenderLayerTag.serializer()))
        }

        // Data class hierarchy used for AnimationScript component
        polymorphic(TweenBase::class) {
            subclass(SpawnNewTweenSequence::class)
            subclass(ParallelTweens::class)
            subclass(Wait::class)
            subclass(Jump::class)
            subclass(SpawnEntity::class)
            subclass(ExecuteConfigFunction::class)
            subclass(DeleteEntity::class)
            subclass(TweenRgba::class)
            subclass(TweenPosition::class)
            subclass(TweenMotion::class)
            subclass(TweenSprite::class)
            subclass(TweenSwitchLayerVisibility::class)
            subclass(TweenSpawner::class)
            subclass(TweenSound::class)
            subclass(TweenTextField::class)
            subclass(TweenTouchInput::class)
        }
    }
}

/**
 * A special serializer to prohibit serialization of FloatArray in Parallax plane config.
 * For some reason @EncodeDefault(NEVER) does not work.
 */
object ParallaxSpeedFactors : KSerializer<FloatArray> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ParallaxSpeedFactors", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: FloatArray) = encoder.encodeInt(0)
    override fun deserialize(decoder: Decoder): FloatArray = floatArrayOf()
}

/**
 * A serializer strategy for Korge [HorizontalAlign] type. The alignment ratio will be saved as double.
 */
object HorizontalAlignAsDouble : KSerializer<HorizontalAlign> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HorizontalAlignAsDouble", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: HorizontalAlign) = encoder.encodeDouble(value.ratio)
    override fun deserialize(decoder: Decoder): HorizontalAlign = HorizontalAlign(decoder.decodeDouble())
}
object HAlignAsString : KSerializer<HorizontalAlign> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("HAlignAsString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: HorizontalAlign) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): HorizontalAlign = HorizontalAlign(decoder.decodeString())
}

/**
 * A serializer strategy for Korge [VerticalAlign] type. The alignment ratio will be saved as double.
 */
object VerticalAlignAsDouble : KSerializer<VerticalAlign> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("VerticalAlignAsDouble", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: VerticalAlign) = encoder.encodeDouble(value.ratio)
    override fun deserialize(decoder: Decoder): VerticalAlign = VerticalAlign(decoder.decodeDouble())
}
object VAlignAsString : KSerializer<VerticalAlign> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("VAlignAsString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: VerticalAlign) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): VerticalAlign = VerticalAlign(decoder.decodeString())
}

/**
 * A serializer strategy for Korge [RGBA] type. The color value is saved as integer number.
 */
object RGBAAsInt : KSerializer<RGBA> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RGBAAsInt", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: RGBA) = encoder.encodeInt(value.value)
    override fun deserialize(decoder: Decoder): RGBA = RGBA(decoder.decodeInt())
}

/**
 * A serializer strategy for Korge [RGBA] type. The color value is saved as hex String (#xxxxxxxx).
 */
object RGBAAsString : KSerializer<RGBA> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RGBAAsString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: RGBA) = encoder.encodeString(value.hexString)
    override fun deserialize(decoder: Decoder): RGBA = RGBA(
        decoder.decodeString().substr(1, 2).toInt(16),
        decoder.decodeString().substr(3, 2).toInt(16),
        decoder.decodeString().substr(5, 2).toInt(16),
        decoder.decodeString().substr(7, 2).toInt(16),
    )
}

/**
 * A simple serializer strategy for Korge [Easing] types. It serializes the easing class name as string.
 */
object EasingAsString : KSerializer<Easing> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EasingAsString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Easing) =
        encoder.encodeString(value::class.toString().substringAfter('$'))

    override fun deserialize(decoder: Decoder): Easing = Easing.ALL[decoder.decodeString()] ?:
        throw SerializationException("EasingAsString: No Easing type for '${decoder.decodeString()}' found in decoder!")
}

/**
 * A simple "Any" object serializer which encodes the Any value as a String within a data class container.
 *
 * Currently, not used in Korge-fleks.
 */
object AnyAsString : KSerializer<Any> {
    override val descriptor: SerialDescriptor = ContainerForAny.serializer().descriptor

    @Serializable
    data class ContainerForAny(val type: String, val value: String)

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is String -> ContainerForAny.serializer().serialize(encoder, ContainerForAny("String", value.toString()))
            is Double -> ContainerForAny.serializer().serialize(encoder, ContainerForAny("Double", value.toString()))
            is RgbaComponent.Rgb -> ContainerForAny.serializer().serialize(encoder, ContainerForAny("Rgb", value.toString()))
            else -> throw SerializationException("AnySerializer: No rule to serialize type '${value::class}'!")
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val containerForAny = decoder.decodeSerializableValue(ContainerForAny.serializer())
        return when (containerForAny.type) {
            "String" -> containerForAny.value
            "Double" -> containerForAny.value.toDouble()
            "Rgb" -> RgbaComponent.Rgb.fromString(containerForAny.value)
            else -> throw SerializationException("AnySerializer: No rule to deserialize type '${containerForAny.type}'!")
        }
    }
}

/**
 * An even simpler "Any" object serializer which encodes a given Any value as specific typed property
 * within a data class container.
 */
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = ContainerForAny.serializer().descriptor

    // The trick here is that only one property shall be non-null. Thus, it contains the type information
    // implicitly during the serialization process. Default null properties will not be serialized explicitly.
    // Thus, no unnecessary clutter is added to the json string.
    @Serializable
    data class ContainerForAny(
        val double: Double? = null,
        val float: Float? = null,
        val int: Int? = null,
        val string: String? = null,
        val boolean: Boolean? = null,
        val rgb: RgbaComponent.Rgb? = null,
        val direction: ImageAnimation.Direction? = null
    )

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is Double -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(double = value))
            is Float -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(float = value))
            is Int -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(int = value))
            is String -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(string = value))
            is Boolean -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(boolean = value))
            is RgbaComponent.Rgb -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(rgb = value))
            is ImageAnimation.Direction -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(direction = value))
            else -> throw SerializationException("AnySerializer: No rule to serialize type '${value::class}'!")
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val containerForAny = decoder.decodeSerializableValue(ContainerForAny.serializer())
        return (containerForAny.double ?: containerForAny.float ?: containerForAny.int ?:
                containerForAny.string ?: containerForAny.boolean ?: containerForAny.rgb ?:
                containerForAny.direction ?:
                throw SerializationException("AnySerializer: No non-null property in ContainerForAny found!"))
    }
}

/**
 * A serializer which supports registering [Enum]s as subclasses in polymorphic serialization when class discriminators are used.
 * When class discriminators are used, an enum is not encoded as a structure which the class discriminator can be added to.
 * An exception is thrown when initializing Json: "Serializer for <enum> of kind ENUM cannot be serialized polymorphically with class discriminator."
 * This serializer encodes the enum as a structure with a single `value` holding the enum value.
 *
 * Use this serializer to register the enum in the serializers module, e.g.:
 * `subclass( <enum>::class, PolymorphicEnumSerializer( <enum>.serializer() )`
 *
 * This serializer is used for serializing enum tags from Fleks.
 */
@OptIn( ExperimentalSerializationApi::class )
class PolymorphicEnumSerializer<T : Enum<T>>( private val enumSerializer: KSerializer<T>) : KSerializer<T>
{
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor( enumSerializer.descriptor.serialName )
    {
        element( "value", enumSerializer.descriptor )
    }

    override fun deserialize( decoder: Decoder): T =
        decoder.decodeStructure( descriptor )
        {
            decodeElementIndex( descriptor )
            decodeSerializableElement( descriptor, 0, enumSerializer )
        }

    override fun serialize(encoder: Encoder, value: T ) =
        encoder.encodeStructure( descriptor )
        {
            encodeSerializableElement( descriptor, 0, enumSerializer, value )
        }
}
