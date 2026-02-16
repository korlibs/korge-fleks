package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.encodeToString
import kotlin.test.assertFalse


data class TestEntityConfig(
    override val name: String
) : EntityConfig {
    override fun World.entityConfigure(entity: Entity) : Entity {
        return Entity(id = 8080, version = 0u)
    }

    init {
        EntityFactory.register(this)
    }
}

object CommonTestEnv {
    private val snapshotSerializer = SnapshotSerializer()

    @Suppress("UNCHECKED_CAST")
    fun serializeDeserialize(worldIn: World, worldOut: World, printout: Boolean = false) {
        val compactJson = snapshotSerializer.json(pretty = true).encodeToString(worldIn.snapshot())
        if (printout) println("compact: $compactJson")
        val snapshotFromJson: Map<Entity, Snapshot> = snapshotSerializer.json().decodeFromString(compactJson)
        worldOut.loadSnapshot(snapshotFromJson)

        // Check if there is any component not having short SerialName set
        assertFalse(
            compactJson.contains("korlibs"), "serializeDeserialize: json string should not contain full class" +
                    "names like 'com.soywiz.korgeFleks.components...'. Please add @SerialName(...) to the Component class! json: \n\n$compactJson\n"
        )

        if (printout) {
            println("\nexpected object: ${worldIn.snapshot()}")
            println("decoded object:  ${worldOut.snapshot()}\n")
        }
    }
}
