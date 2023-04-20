package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlin.test.assertFalse


fun World.testInvokable(entity: Entity) : Entity {
    println("Invoke test - configureTestEntity: world: $this, entity: ${entity.id}")
    return entity
}

// This test configure function creates a new entity and sets its id
fun World.testFunction(entity: Entity) : Entity {
    return Entity(id = 8080)
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
