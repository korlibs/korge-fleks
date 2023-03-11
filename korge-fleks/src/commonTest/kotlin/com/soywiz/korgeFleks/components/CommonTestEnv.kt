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
    var id: Int = 0
) : Config

fun World.testInvokable(entity: Entity, config: Config) : Entity {
    println("Invoke test - configureTestEntity: world: $this, entity: ${entity.id}, config: $config")
    return entity
}

// This test configure function creates a new entity and sets its id
fun World.testFunction(entity: Entity, config: Config) : Entity {
    config as TestConfig
    return Entity(id = config.id)
}

object CommonTestEnv {
    val snapshotSerializer = SnapshotSerializer()

    @Suppress("UNCHECKED_CAST")
    fun serializeDeserialize(worldIn: World, worldOut: World, printout: Boolean = false) {
        val compactJson = snapshotSerializer.json().encodeToString(worldIn.snapshot() as SerializableSnapshot)
        if (printout) println("compact: $compactJson")
        val snapshotFromJson = snapshotSerializer.json().decodeFromString<SerializableSnapshot>(compactJson)
        worldOut.loadSnapshot(snapshotFromJson as FleksSnapshot)

        // Check if there is any component not having short SerialName set
        assertFalse(
            compactJson.contains("soywiz"), "serializeDeserialize: json string should not contain full class" +
                    "names like 'com.soywiz.korgeFleks.components...'. Please add @SerialName(...) to the Component class! json: \n\n$compactJson\n"
        )

        if (printout) {
            println("\nexpected object: ${worldIn.snapshot()}")
            println("decoded object:  ${worldOut.snapshot()}\n")
        }
    }
}
