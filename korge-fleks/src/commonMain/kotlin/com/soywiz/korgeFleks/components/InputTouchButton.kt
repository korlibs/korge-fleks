package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InputTouchButton")
data class InputTouchButton(
    var pressed: Boolean = false,
    var triggerImmediately: Boolean = false,
    @Serializable(InvokableSerializer::class)
    var action: Invokable = World::noFunction
) : Component<InputTouchButton>, SerializeBase {
    override fun type(): ComponentType<InputTouchButton> = InputTouchButton
    companion object : ComponentType<InputTouchButton>()
}
