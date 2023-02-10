package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.soywiz.korgeFleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LifeCycle")
data class LifeCycle(
    var healthCounter: Int = 100
) : Component<LifeCycle>, SerializeBase {
    override fun type(): ComponentType<LifeCycle> = LifeCycle
    companion object : ComponentType<LifeCycle>()
}
