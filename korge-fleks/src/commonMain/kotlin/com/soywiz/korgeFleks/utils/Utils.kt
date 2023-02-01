package com.soywiz.korgeFleks.utils

import com.soywiz.korge.tween.V2
import kotlin.random.Random
import kotlin.reflect.KMutableProperty0

fun ClosedFloatingPointRange<Double>.random() = Random.nextDouble(start, endInclusive)
fun ClosedFloatingPointRange<Float>.random() = Random.nextDouble(start.toDouble(), endInclusive.toDouble()).toFloat()

fun random(radius: Double) = (-radius..radius).random()

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

inline operator fun KMutableProperty0<String>.get(end: String) = V2(this, this.get(), end, ::interpolateString, includeStart = false)
inline operator fun KMutableProperty0<String>.get(initial: String, end: String) = V2(this, initial, end, ::interpolateString, includeStart = true)
inline operator fun KMutableProperty0<Boolean>.get(end: Boolean) = V2(this, this.get(), end, ::interpolateBoolean, includeStart = false)
inline operator fun KMutableProperty0<Boolean>.get(initial: Boolean, end: Boolean) = V2(this, initial, end, ::interpolateBoolean, includeStart = true)
