package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*
import korlibs.image.color.*
import korlibs.image.text.*
import kotlin.jvm.*
import kotlin.random.Random


fun ClosedFloatingPointRange<Double>.random() = Random.nextDouble(start, endInclusive)
fun ClosedFloatingPointRange<Float>.random() = Random.nextDouble(start.toDouble(), endInclusive.toDouble()).toFloat()

fun random(radius: Double) = (-radius..radius).random()
fun random(radius: Float) = (-radius..radius).random()

/**
 * Increment float value and wrap around at max and min.
 */
fun Float.wrapInc(amount: Float, max: Float, min: Float = 0f): Float =
    (this + amount).let { if (it >= max) it - max else if (it < min) it + max else it }

@PublishedApi
internal fun interpolateString(ratio: Double, l: String, r: String): String = when {
    ratio < 0.5 -> l
    else -> r
}

@PublishedApi
internal fun interpolateBoolean(ratio: Double, l: Boolean, r: Boolean): Boolean = when {
    ratio < 0.5 -> l
    else -> r
}

/**
 * Clone function for a Map object with [CloneableData] values.
 */
@JvmName("MapOfCloneableData")
fun<K, T> Map<K, CloneableData<T>>.clone() : Map<K, T> {
    val mapCopy = mutableMapOf<K, T>()
    forEach { (key, value) ->
        // Perform deep copy of map value elements
        mapCopy[key] = value.clone()
    }
    return mapCopy
}

/**
 * Clone function for a Map object with List values.
 */
@JvmName("MapOfListCloneableData")
fun<K, T> Map<K, List<CloneableData<T>>>.clone() : Map<K, List<T>> {
    val mapCopy = mutableMapOf<K, List<T>>()
    forEach { (key, value) ->
        // Perform deep copy of map value elements
        mapCopy[key] = value.clone()
    }
    return mapCopy
}

/**
 * Clone function for a Map object with [Entity] values.
 */
@JvmName("MapOfEnities")
fun<T> Map<T, Entity>.clone() : Map<T, Entity> {
    val mapCopy = mutableMapOf<T, Entity>()
    forEach { (key, value) ->
        // Perform deep copy of map value elements
        mapCopy[key] = value.clone()
    }
    return mapCopy
}

/**
 * Clone function (deep copy) for [HorizontalAlign] enum objects.
 */
fun HorizontalAlign.clone() : HorizontalAlign = HorizontalAlign(this.ratio)

/**
 * Clone function (deep copy) for [VerticalAlign] enum objects.
 */
fun VerticalAlign.clone() : VerticalAlign = VerticalAlign(this.ratio)

/**
 * Clone function (deep copy) for [RGBA] objects.
 */
fun RGBA.cloneRgba() : RGBA = RGBA(this.value)


/**
 * Init function (deep copy) for [MutableList] of String elements.
 */
fun MutableList<String>.init(from: List<String>) {
    this.addAll(from)
}

/**
 * Init function (deep copy) for [MutableList] of [Entity] elements.
 * This works because Entities are static data classes which will not be "reused" with other
 * id and version.
 */
fun MutableList<Entity>.init(from: List<Entity>) {
    this.addAll(from)
}

/**
 * Init function (deep copy) for [MutableList] of [Poolable] elements.
 * Elements are taken from their respective pools by cloning them in the scope of the world where the pool was added.
 */
fun <T> MutableList<Poolable<T>>.init(world: World, from: List<Poolable<T>>) {
    from.forEach { item ->
        this.add(item.run { world.clone() } )
    }
}

fun MutableMap<String, Entity>.init(from: Map<String, Entity>) {
    this.putAll(from)
}

fun MutableList<String>.init(from: List<String>) {
    this.addAll(from)
}