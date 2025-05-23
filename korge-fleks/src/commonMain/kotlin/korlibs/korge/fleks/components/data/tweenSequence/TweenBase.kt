package korlibs.korge.fleks.components.data.tweenSequence

import com.github.quillraven.fleks.Entity
import korlibs.math.interpolation.Easing


/**
 * All Tweens must implement this interface.
 */
interface TweenBase {
    var target: Entity
    var delay: Float?
    var duration: Float?
    var easing: Easing?
    fun free()
}

/**
 * All Tweens which contain a list of tweens must implement this interface.
 */
interface TweenListBase {
    val tweens: MutableList<TweenBase>
    fun freeRecursive()
}

/**
 * Call free for all tweens of a Tween-List.
 */
fun List<TweenBase>.free() {
    forEach { tween ->
        tween.free()
    }
}
