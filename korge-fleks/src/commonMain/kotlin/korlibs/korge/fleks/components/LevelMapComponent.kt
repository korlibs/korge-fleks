package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.componentPool.*
import korlibs.korge.fleks.utils.poolableData.*
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
data class LevelMap private constructor(
    var levelName: PoolableString = PoolableString.EMPTY,
    val layerNames: MutableList<PoolableString> = mutableListOf(),
) : PoolableComponent<LevelMap>() {
    override fun type() = LevelMapComponent

    companion object {
        val LevelMapComponent = componentTypeOf<LevelMap>()

        fun World.LevelMapComponent(config: LevelMap.() -> Unit ): LevelMap =
            getPoolable(LevelMapComponent).apply { config() }

        fun InjectableConfiguration.addLevelMapComponentPool(preAllocate: Int = 0) {
            addPool(LevelMapComponent, preAllocate) { LevelMap() }
        }
    }

    override fun World.clone(): LevelMap =
        getPoolable(LevelMapComponent).apply {
            levelName = this@LevelMap.levelName
            layerNames.clone(this@LevelMap.layerNames)
        }

    override fun reset() {
        // level name will be set on initialization of the component

        // TODO: Check why this does not work
//        layerNames.clear()  // Make list empty for reuse - Strings are owned by the StringPool
    }
}

fun MutableList<PoolableString>.clone(other: MutableList<PoolableString>) {
    this.addAll(other)
}

/*
@Serializable @SerialName("LevelMap")
data class LevelMapComponent(
) : CloneableComponent<LevelMapComponent>() {
    override fun type(): ComponentType<LevelMapComponent> = LevelMapComponent
    companion object : ComponentType<LevelMapComponent>()

    // Author's hint: Check if deep copy is needed on any change in the component!
    override fun clone(): LevelMapComponent =
        LevelMapComponent(
            levelName = levelName,
            layerNames = layerNames.clone()
        )
}
*/
