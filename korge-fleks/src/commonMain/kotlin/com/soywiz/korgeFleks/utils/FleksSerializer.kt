package com.soywiz.korgeFleks.utils

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.Entity
import com.soywiz.kds.fastCastTo
import com.soywiz.korgeFleks.korlibsAdaptation.Json
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

interface SerializeFleksComponent : Json.CustomSerializer, FleksComponentDecoder

/**
 * Interface to populate a component from a [String Map of Any?][FleksJsonComponent].
 */
interface FleksComponentDecoder {
    fun decodeFromJson(v: FleksJsonComponent)
}

/**
 * Generic component encoder
 */
inline fun <reified T: Any?> Component<*>.encodeComponent(b: StringBuilder, vararg properties: KProperty0<T>) {
    // get and write component name to json string
    b.append("{\"${this.type()::class.toString().substringAfterLast(".").substringBefore("$")}\":{")
    // get and write (stringify) all given properties of the component to json string
    properties.forEachIndexed { index, property ->
        if (index != 0) b.append(',')
        b.append("\"${property.name}\":${Json.stringify(property.get())}")
    }
    b.append("}}")
}

/**
 * Component decoders for specific types
 */
inline fun <reified T: Any?> FleksJsonComponent.decodeComponent(vararg properties: KMutableProperty0<T>) =
    properties.forEach { property -> property.set(this[property.name].fastCastTo()) }  // No idea if this is safe - but it works
inline fun FleksJsonComponent.decodeEntityComponent(property: KMutableProperty0<Entity>) =
    property.set(Entity(this[property.name].toString().toInt()))
inline fun FleksJsonComponent.decodeEntityMapComponent(property: KMutableProperty0<MutableMap<String, Entity>>) =
    property.set((this[property.name] as MutableMap<String, String>).mapValues { Entity(id = it.value.toInt()) }.toMutableMap())

// Typedefs for Fleks snapshot functions results
typealias FleksEntitySnapshot = List<Component<*>>  // snapshotOf()
typealias FleksWorldSnapshot = Map<Entity, FleksEntitySnapshot>  // snapshot()

// Typedefs for KorIO Json elements of serializing snapshot and snapshotOf results
//                        {   Entity: [   { Component: {   property: ANY } } ] }
typealias FleksJsonComponent =                         Map<String,   Any?>
typealias FleksSnapshot = Map<String, List<Map<String, FleksJsonComponent  > > >
typealias FleksSnapshotOf =           List<Map<String, FleksJsonComponent  > >
