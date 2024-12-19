package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*

object EntityByName {
    lateinit var world: World

    private val map = mutableMapOf<String, Entity>()

    fun add(name: String, entity: Entity) {
//        if (map.containsKey(name)) println("EntityByName: Warning! Entity with name '$name' already exists! Old entity will be overwritten.")
        // Do not overwrite already created objects with same name (e.g. created objects by spawner)
        if (!map.containsKey(name)) map[name] = entity
    }

    fun get(name: String): Entity {
        return if (!map.containsKey(name)) {
            throw Error("ERROR: EntityByName: Entity with name '$name' does not exist!")
        } else map[name]!!
    }

    fun getOrNull(name: String): Entity? = map[name]

    fun traceOnce(name: String) {
        if (map.containsKey(name)) {
            val entityComponents = world.snapshotOf(map[name]!!)
            // place break point on next line
            remove(name)
        }
    }

    fun trace(name: String) {
        if (map.containsKey(name)) {
            val entityComponents = world.snapshotOf(map[name]!!)
            // place break point on next line
            println(entityComponents)
        }
    }


    fun getComponents(name: String): Snapshot? = with(world) {
        return if (!map.containsKey(name)) {
            println("EntityByName: Entity with name '$name' does not exist! Cannot show it!")
            null
        } else snapshotOf(map[name]!!)
    }

    fun contains(name: String): Boolean = map.containsKey(name)

    fun remove(name: String) {
//        if (!map.containsKey(name)) println("EntityByName: Entity with name '$name' does not exist!")
//        else map.remove(name)
        map.remove(name)
    }
}
