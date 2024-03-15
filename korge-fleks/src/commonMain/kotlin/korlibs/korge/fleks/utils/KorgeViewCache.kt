package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.Entity
import korlibs.korge.parallax.ImageDataViewEx
import korlibs.korge.view.View
import korlibs.korge.parallax.ParallaxDataView
import kotlin.concurrent.*
import kotlin.math.max


/**
 * This object stores KorGE specific internal objects which we do not want to store in the components.
 * They are accessed by entity id the same way as components.
 */
object KorgeViewCache {

    @Volatile
    private lateinit var views: Array<View?>

    fun createInstance(arraySize: Int = 64) {
        views = Array(arraySize) { null }
    }

    fun addOrUpdate(entity: Entity, view: View) {
        if (entity.id >= views.size) {
            views = views.copyOf(max(views.size * 2, entity.id + 1))
        }
        views[entity.id] = view
    }

    fun remove(entity: Entity) {
        if (views.size > entity.id) {
            views[entity.id] = null
        } else error("KorgeViewCache: Entity '${entity.id}' is out of range on remove!")
    }

    operator fun get(entity: Entity) : View {
        return if (views.size > entity.id) {
            views[entity.id] ?: error("KorgeViewCache: View of entity '${entity.id}' is null!")
        } else error("KorgeViewCache: Entity '${entity.id}' is out of range on get!")
    }

    fun getOrNull(entity: Entity) : View? {
        return if (views.size > entity.id) views[entity.id]  // Cache potentially has this view. However, return value can still be null!
        else null
    }

    fun getLayer(entity: Entity, name: String) : View {
        return when (val view = get(entity)) {
            is ImageDataViewEx -> view.getLayer(name)
                ?: error("KorgeViewCache: Could not find layer '$name' from ImageAnimView entity '${entity.id}'!")
            is ParallaxDataView -> view.getLayer(name)
                ?: error("KorgeViewCache: Could not find layer '$name' from ParallaxDataView entity '${entity.id}'!")
            else -> error("KorgeViewCache: View does not have getLayer function!")
        }
    }
}
