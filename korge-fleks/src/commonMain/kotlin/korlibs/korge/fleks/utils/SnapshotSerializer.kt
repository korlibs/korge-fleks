package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.assets.AssetStore
import korlibs.korge.fleks.entity.config.Invokable
import korlibs.math.interpolation.Easing
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
 * All Fleks components which should be serializable needs to derive from this interface.
 */
interface SerializeBase

// Some convenience aliases for Fleks snapshots of world and entities
typealias SerializableSnapshot = Map<Entity, List<SerializeBase>>  // snapshot of Fleks world
typealias SerializableSnapshotOf = List<SerializeBase>  // snapshot of one entity
typealias FleksSnapshot = Map<Entity, List<Component<*>>>  // snapshot data of Fleks world
typealias FleksSnapshotOf = List<Component<*>>  // snapshot data of one entity

/**
 * Class for serializing identifier objects for entity configs and functions in components.
 *
 * These identifiers are used to access a specific entity configuration from the [AssetStore].
 * They are also used to access a specific lambda function through the [Invokable] object.
 * It wraps a string value. Using this [Identifier] object everywhere in the code is more error-prone than using a plain string.
 * Since strings can have typos which are not realized at compile time.
 */
@JvmInline @Serializable
value class Identifier(val name: String)

/**
 * This polymorphic module config for kotlinx serialization lists all Korge-fleks
 * internal components as subclasses.
 */
internal val internalModule = SerializersModule {
    // Top level component classes
    polymorphic(SerializeBase::class) {
        subclass(AnimateComponent::class)
        subclass(AnimationScript::class)
        subclass(Info::class)
        subclass(Drawable::class)
        subclass(Appearance::class)
        subclass(Rgb::class)
        subclass(SpecificLayer::class)
        subclass(SwitchLayerVisibility::class)
        subclass(LayerVisibility::class)
        subclass(InputTouchButton::class)
        subclass(Layout::class)
        subclass(LifeCycle::class)
        subclass(Parallax::class)
        subclass(ParallaxMotion::class)
        subclass(PositionShape::class)
        subclass(Offset::class)
        subclass(OffsetByFrameIndex::class)
        subclass(NoisyMove::class)
        subclass(Point::class)
        subclass(Motion::class)
        subclass(Rigidbody::class)
        subclass(Sound::class)
        subclass(Spawner::class)
        subclass(Sprite::class)
        subclass(SubEntities::class)
        subclass(Text::class)
        subclass(MultiLineText::class)
        subclass(TiledMap::class)
    }
    // Data class hierarchy used for AnimationScript component
    polymorphic(TweenBase::class) {
        subclass(TweenSequence::class)
        subclass(ParallelTweens::class)
        subclass(Wait::class)
        subclass(SpawnEntity::class)
        subclass(ExecuteConfigFunction::class)
        subclass(DeleteEntity::class)
        subclass(TweenAppearance::class)
        subclass(TweenPositionShape::class)
        subclass(TweenOffset::class)
        subclass(TweenLayout::class)
        subclass(TweenSprite::class)
        subclass(TweenSwitchLayerVisibility::class)
        subclass(TweenNoisyMove::class)
        subclass(TweenSpawner::class)
        subclass(TweenSound::class)
    }
}

/**
 * TODO document class here
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

    fun json(pretty: Boolean = false) : Json {
        if (dirty) {
            var modules = internalModule
            modulesMap.values.forEach { module ->
                modules = modules.plus(module)
            }

            json = Json {
                prettyPrint = pretty
                serializersModule = modules
            }
            dirty = false
        }
        return json
    }
}

/**
 * A simple serializer strategy for Easing types. It serializes the easing class name as string.
 */
object EasingSerializer : KSerializer<Easing> {
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
            is Rgb -> ContainerForAny.serializer().serialize(encoder, ContainerForAny("Rgb", value.toString()))
            else -> throw SerializationException("AnySerializer: No rule to serialize type '${value::class}'!")
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val containerForAny = decoder.decodeSerializableValue(ContainerForAny.serializer())
        return when (containerForAny.type) {
            "String" -> containerForAny.value
            "Double" -> containerForAny.value.toDouble()
            "Rgb" -> Rgb.fromString(containerForAny.value)
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
        val rgb: Rgb? = null
    )

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is Double -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(double = value))
            is Float -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(float = value))
            is Int -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(int = value))
            is String -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(string = value))
            is Boolean -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(boolean = value))
            is Rgb -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(rgb = value))
            else -> throw SerializationException("AnySerializer: No rule to serialize type '${value::class}'!")
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val containerForAny = decoder.decodeSerializableValue(ContainerForAny.serializer())
        return (containerForAny.double ?: containerForAny.float ?: containerForAny.int ?:
                containerForAny.string ?: containerForAny.boolean ?: containerForAny.rgb ?:
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
 * Currently, not used in Korge-fleks.
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
