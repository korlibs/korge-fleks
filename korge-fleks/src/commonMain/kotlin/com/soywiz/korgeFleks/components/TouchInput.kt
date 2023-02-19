package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class TouchInput(
    var drawOnLayer: String = ""
) : Component<TouchInput> {
    override fun type(): ComponentType<TouchInput> = TouchInput
    companion object : ComponentType<TouchInput>()
}
