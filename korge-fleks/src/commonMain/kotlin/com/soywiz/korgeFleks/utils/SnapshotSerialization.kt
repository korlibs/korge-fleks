package com.soywiz.korgeFleks.utils

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.entity.config.Config
import com.soywiz.korma.interpolation.Easing
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * All Fleks components which should be serializable needs to derive from this interface.
 */
interface SerializeBase

typealias SerializableSnapshot = Map<Entity, List<SerializeBase>>
typealias SerializableSnapshotOf = List<SerializeBase>
typealias FleksSnapshot = Map<Entity, List<Component<*>>>
typealias FleksSnapshotOf = List<Component<*>>

/**
 * This polymorphic module config for kotlinx serialization lists all Korge-fleks
 * internal components as subclasses.
 */
val componentModule = SerializersModule {
    // Top level component classes
    polymorphic(SerializeBase::class) {
        subclass(AnimateComponent::class)
        subclass(AnimationScript::class)
        subclass(DebugInfo::class)
        subclass(AssetReload::class)
        subclass(PositionShape::class)
        subclass(Offset::class)
    }
    // Data class hierarchy used for AnimationScript component
    polymorphic(TweenBase::class) {
        subclass(TweenSequence::class)
        subclass(ParallelTweens::class)
        subclass(Wait::class)
    }
    // Data class hierarchy used for AnimationScript component
    polymorphic(TweenBaseHasEntity::class) {
        subclass(SpawnEntity::class)
        subclass(DeleteEntity::class)
        subclass(TweenAppearance::class)
        subclass(TweenPositionShape::class)
        subclass(TweenOffset::class)
        subclass(TweenSprite::class)
        subclass(TweenSwitchLayerVisibility::class)
        subclass(TweenSpawner::class)
        subclass(TweenSound::class)
    }
    polymorphic(Invokable::class) {
        subclass(EmptyInvokable::class)
    }
}

/**
 * This is the type for component properties which should contain invokable functions. Those functions
 * are used e.g. to specifically configure new created generic entities.
 */
interface Invokable {
    fun invoke(world: World, entity: Entity, config: Config)
}

// This object can be used to initialize [Invokable] properties of components.
@Serializable
object EmptyInvokable : Invokable { override fun invoke(world: World, entity: Entity, config: Config) {} }

/**
 * A simple serializer strategy for Entity type. It serializes the Entity value class name as string.
 */
object EntityAsInt : KSerializer<Entity> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("EntityAsInt", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: Entity) = encoder.encodeInt(value.id)
    override fun deserialize(decoder: Decoder): Entity = Entity(decoder.decodeInt())
}

/**
 * A simple serializer strategy for Easing types. It serializes the easing class name as string.
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
object AnyAsTypedContainer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = ContainerForAny.serializer().descriptor

    // The trick here is that only one property shall be non-null. Thus, it contains the type information
    // implicitly during the serialization process. Default null properties will not be serialized explicitly.
    // Thus, no unnecessary clutter is added to the json string.
    @Serializable
    data class ContainerForAny(
        val double: Double? = null,
        val int: Int? = null,
        val string: String? = null,
        val boolean: Boolean? = null,
        val rgb: Rgb? = null
    )

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is Double -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(double = value))
            is Int -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(int = value))
            is String -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(string = value))
            is Boolean -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(boolean = value))
            is Rgb -> ContainerForAny.serializer().serialize(encoder, ContainerForAny(rgb = value))
            else -> throw SerializationException("AnySerializer: No rule to serialize type '${value::class}'!")
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        val containerForAny = decoder.decodeSerializableValue(ContainerForAny.serializer())
        return (containerForAny.double ?:
                containerForAny.int ?:
                containerForAny.string ?:
                containerForAny.boolean ?:
                containerForAny.rgb ?:
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
