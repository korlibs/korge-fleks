package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.Entity
import korlibs.korge.fleks.utils.Poolable
import korlibs.math.interpolation.Easing


/**
 * All tweens must implement this interface and [Poolable] interface!!!
 */
interface TweenBase {
    var target: Entity
    var delay: Float?
    var duration: Float?
    var easing: Easing?
}

/**
 * All tweens which contain a list of tweens must implement this interface in addition to
 * [TweenBase] and [Poolable] interfaces!!!
 */
interface TweenListBase {
    val tweens: MutableList<TweenBase>
}

/**
 * This function is used to initialize a list of tweens with data from another list.
 * It clones each tween from the source list and adds it to the target list.
 */
fun MutableList<TweenBase>.init(from: List<TweenBase>) {
    from.forEach { tween ->
        val newTween = (tween as Poolable<*>).clone()
        this.add(newTween as TweenBase)
    }
}

/**
 * This function is used to free and clear a list of tweens.
 * It iterates through each tween in the list, frees it, and then clears the list.
 */
fun MutableList<TweenBase>.freeAndClear() {
    this.forEach { tween ->
        (tween as Poolable<*>).free()
    }
    this.clear()
}
