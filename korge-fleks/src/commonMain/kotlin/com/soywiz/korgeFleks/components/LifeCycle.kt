package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class LifeCycle(
    var healthCounter: Int = 100
) : Component<LifeCycle> {
    override fun type(): ComponentType<LifeCycle> = LifeCycle
    companion object : ComponentType<LifeCycle>()
}
