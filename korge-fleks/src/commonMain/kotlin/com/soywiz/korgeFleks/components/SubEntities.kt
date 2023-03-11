package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.Entity
import com.soywiz.korgeFleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SubEntities")
data class SubEntities(
    var entities: MutableMap<String, Entity> = mutableMapOf()
) : Component<SubEntities>, SerializeBase {
    override fun type(): ComponentType<SubEntities> = SubEntities
    companion object : ComponentType<SubEntities>()

    operator fun get(name: String) : Entity = entities[name] ?: error("SubEntities: Entity with name '$name' not found!")
}
