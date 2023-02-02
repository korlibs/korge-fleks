package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity

data class SubEntities(
    var entities: MutableMap<String, Entity> = mutableMapOf()
) : Component<SubEntities> {
    override fun type(): ComponentType<SubEntities> = SubEntities
    companion object : ComponentType<SubEntities>()

    operator fun get(name: String) : Entity {
        return entities[name] ?: error("SubEntities: Entity with name '$name' not found!")
    }
}
