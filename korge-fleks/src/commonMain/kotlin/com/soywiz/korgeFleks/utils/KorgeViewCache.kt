package com.soywiz.korgeFleks.utils

import com.github.quillraven.fleks.Entity
import com.soywiz.korge.view.View
import kotlin.math.max

class KorgeViewCache(arraySize: Int = 64) {
    // Korge specific internal objects which we do not want to store in the components - they are accessed by entity id the same way as components
    private var views: Array<View?> = Array(arraySize) { null }

    fun addOrUpdate(entity: Entity, view: View) {
        if (entity.id >= views.size) {
            views = views.copyOf(max(views.size * 2, entity.id + 1))
        }
        views[entity.id] = view
    }

    operator fun get(entity: Entity) : View {
        return if (views.size > entity.id) {
            views[entity.id] ?: error("KorgeViewCache: View of entity '${entity.id}' is null!")
        } else error("KorgeViewCache: Entity '${entity.id}' is out of range!")
    }

    fun getOrNull(entity: Entity) : View? {
        return if (views.size > entity.id) views[entity.id]  // Cache potentially has this view. However, return value can still be null!
        else null
    }
}

class AssetReloadCache {
    val backgroundEntities = mutableSetOf<Entity>()
    val spriteEntities = mutableSetOf<Entity>()
}
