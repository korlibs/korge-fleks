package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.Entity

class EntityByName {
    private val map = mutableMapOf<String, Entity>()

    fun add(name: String, entity: Entity) {
        if (map.containsKey(name)) println("EntityByName: Warning! Entity with name '$name' already exists! Old entity will be overwritten.")
        map[name] = entity
    }

    fun remove(name: String) {
        if (!map.containsKey(name)) println("EntityByName: Entity with name '$name' does not exist!")
        else map.remove(name)
    }
}
