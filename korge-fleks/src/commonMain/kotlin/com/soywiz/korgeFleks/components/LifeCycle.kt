package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LifeCycle")
data class LifeCycle(
    var healthCounter: Int = 100
) : Component<LifeCycle> {
    override fun type(): ComponentType<LifeCycle> = LifeCycle
    companion object : ComponentType<LifeCycle>()
}
