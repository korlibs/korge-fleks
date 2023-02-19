package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korio.serialization.json.Json
import com.soywiz.korio.serialization.json.Json.CustomSerializer

data class SubEntities(
    var entities: MutableMap<String, Entity> = mutableMapOf()
) : Component<SubEntities>, CustomSerializer {
    override fun type(): ComponentType<SubEntities> = SubEntities
    companion object : ComponentType<SubEntities>()

    operator fun get(name: String) : Entity = entities[name] ?: error("SubEntities: Entity with name '$name' not found!")
    override fun encodeToJson(b: StringBuilder) {
        b.append("\"SubEntities\": {" )
        stringifyEntities(entities, b)
        b.append(" }")
    }

    private fun stringifyEntities(obj: Map<String, Entity>, b: StringBuilder) {
        for ((i, v) in obj.entries.withIndex()) {
            if (i != 0) b.append(", ")
            Json.stringify(v.key, b)
            b.append(": ")
            b.append("\"Entity\": { \"id\": ${v.value.id} }")
        }
        b.append('}')
    }
}
