import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.world
import com.soywiz.korge.tests.*
import com.soywiz.korgeFleks.components.PositionShape
import com.soywiz.korgeFleks.korlibsAdaptation.Json
import com.soywiz.korgeFleks.utils.*
import kotlin.test.*

class MyTest : ViewsForTesting() {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testComponentSerialization() {
//* Disable KorIO serialization implementation testing

        println("Test component serialization...")

        expectedWorld.entity {
            it += PositionShape(
                x = 5.2,
                y = 42.1,
                entity = Entity(42),
                string = "hello world!",
                notNullString = "hehe",
                entities = mutableMapOf("3" to Entity(3), "hihi" to Entity(1001))
            )
        }

        Json.stringifyPlugin = { obj, b ->
            when(obj) {
                is Entity -> { b.append("\"${obj.id}\""); true }
                else -> false
            }
        }
        Json.stringifyPrettyPlugin = { obj, b ->
            when(obj) {
                is Entity -> { b.inline("\"${obj.id}\""); true }
                else -> false
            }
        }

        val expectedSnapshot = expectedWorld.snapshot()
        val compactJson = Json.stringify(expectedSnapshot)
        val json = Json.parse(compactJson)
        val prettyJson = Json.stringify(json, pretty = true)


        println("\ncompact:\n\n$compactJson")
        println("\npretty:\n\n$prettyJson")
        println("\njson parsed compact:\n\n$json")

        // TODO make decodeFromJson take list of "Any?"

        val componentMapping = mapOf<String, (FleksJsonComponent) -> Component<*>>(
            "PositionShape" to { PositionShape().apply { decodeFromJson(it) } }
        )


        json as FleksSnapshot  // if that cast does not work than we are anyway lost ;-)
        val snapshotFromJson = json.mapKeys {
            Entity(id = it.key.toInt())
        }.mapValues {
            it.value.map { comp ->
                if (comp.entries.size == 1) {
                    val componentName = comp.entries.first().key
                    val componentProps = comp.entries.first().value
                    componentMapping[componentName]!!.invoke(componentProps)
                } else error("Component misalignment - only one component per map allowed!")
            }
        }

        println("\nexpected snapshot:  $expectedSnapshot")
        println("snapshot from Json: $snapshotFromJson\n")

        recreatedWorld.loadSnapshot(snapshotFromJson)
// */
    }
}

