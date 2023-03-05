package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.entity.config.Config
import com.soywiz.korgeFleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.assertFalse

@Serializable
@SerialName("TestConfig")
internal data class TestConfig(
    var centerX: Boolean = false,
    var centerY: Boolean = false,
    var offsetX: Double = 0.0,
    var offsetY: Double = 0.0,
    var logoName: String = "",
    var alpha: Double = 0.0,
    var drawOnLayer: String = ""
) : Config

@Serializable
internal object TestInvokable : Invokable {
    override fun invoke(world: World, entity: Entity, config: Config) = with(world) {
        println("Invoke test - configureTestEntity: world: $world, entity: $entity, config: $config")
    }
}

internal val json = Json {
    prettyPrint = false
    serializersModule = componentModule.plus(
        SerializersModule {
            polymorphic(Config::class) {
                // List here all game specific config classes
                subclass(TestConfig::class)
            }
            polymorphic(SerializeBase::class) {
                // List here all game specific component classes (component classes must derive from SerializeBase interface)
            }
            polymorphic(Invokable::class) {
                subclass(TestInvokable::class)
            }
        }
    )
}

@Suppress("UNCHECKED_CAST")
internal fun serializeDeserialize(worldIn: World, worldOut: World, printout: Boolean = false) {
    val compactJson = json.encodeToString(worldIn.snapshot() as SerializableSnapshot)
    val snapshotFromJson = json.decodeFromString<SerializableSnapshot>(compactJson)
    worldOut.loadSnapshot(snapshotFromJson as FleksSnapshot)

    // Check if there is any component not having short SerialName set
    assertFalse(compactJson.contains("com.soywiz"), "serializeDeserialize: json string should not contain full class" +
            "names like 'com.soywiz.korgeFleks.components...'. Please add @SerialName(...) to the Component class! json: \n\n$compactJson\n")

    if (printout) {
        println("compact: $compactJson")
        println("\nexpected object: ${worldIn.snapshot()}")
        println("decoded object:  ${worldOut.snapshot()}\n")
    }
}
