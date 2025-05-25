package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.assets.ChunkArray2
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to ...
 *
 * @param levelName The unique identifier for the level.
 * @param layerNames List of layer names which shall be drawn by the specific render system.
 *                   Render order is specified by order of strings in the list.
 *                   Example: ["Background", "Playfield", "Collisions"]
 *
 * Author's hint: When adding new properties to the component, make sure to initialize them in the
 *                [reset] function and copy them in the [clone] function.
 */
@Serializable @SerialName("LevelMap")
class LevelMap private constructor(
    var levelName: String = "",
    val layerNames: MutableList<String> = mutableListOf(),

    var levelChunks: ChunkArray2 = ChunkArray2.empty
) : Poolable<LevelMap>() {
    override fun type() = LevelMapComponent

    companion object {
        val LevelMapComponent = componentTypeOf<LevelMap>()

        fun World.LevelMapComponent(config: LevelMap.() -> Unit ): LevelMap =
            getPoolable(LevelMapComponent).apply { config() /*; println("Created: LevelMap '$num'")*/ }

        fun InjectableConfiguration.addLevelMapComponentPool(preAllocate: Int = 0) {
            addPool(LevelMapComponent, preAllocate) { LevelMap(/* num = it */) }
        }
    }

    override fun World.clone(): LevelMap =
        getPoolable(LevelMapComponent).apply {
            levelName = this@LevelMap.levelName
            layerNames.init(from = this@LevelMap.layerNames)
            levelChunks = this@LevelMap.levelChunks.clone()
            //println("Cloned: LevelMap '$num' from '${this@LevelMap.num}'")
        }

    override fun reset() {
        // level name will be set on initialization of the component
        layerNames.clear()  // Make list empty for reuse
        levelChunks = ChunkArray2.empty
        //println("Reset: LevelMap '$num'")
    }
}
