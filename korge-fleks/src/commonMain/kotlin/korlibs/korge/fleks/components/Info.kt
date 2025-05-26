package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * This component is used to add a name to an entity. Useful for making a save-game snapshot readable.
 *
 * Author's hint: When adding new properties to the component, make sure to reset them in the
 *                [cleanupComponent] function and initialize them in the [clone] function.
 */
@Serializable @SerialName("Info")
class Info private constructor(
    //val num: Int = 0,
    var name: String = "noName",
    var entityId: Int = -1,

    // internal
//    var initialized: Boolean = false  -- TODO: remove this if really not needed anymore
) : Poolable<Info>() {
    override fun type(): ComponentType<Info> = InfoComponent

    companion object {
        val InfoComponent = componentTypeOf<Info>()

        fun World.InfoComponent(config: Info.() -> Unit ): Info =
            getPoolable(InfoComponent).apply { config() /*; println("Created: LevelMap '$num'")*/ }

        fun InjectableConfiguration.addInfoComponentPool(preAllocate: Int = 0) {
            addPool(InfoComponent, preAllocate) { Info(/* num = it */) }
        }
    }

    override fun World.clone(): Info =
        getPoolable(InfoComponent).apply {
            name = this@Info.name
            entityId = this@Info.entityId
//            initialized = this@Info.initialized
            //println("Cloned: LevelMap '$num' from '${this@LevelMap.num}'")
        }

    override fun World.initPrefabs(entity: Entity) {
        entityId = entity.id
        EntityByName.add(name, entity)
    }

    override fun World.cleanupPrefabs(entity: Entity) {
        EntityByName.remove(name)
    }
}
