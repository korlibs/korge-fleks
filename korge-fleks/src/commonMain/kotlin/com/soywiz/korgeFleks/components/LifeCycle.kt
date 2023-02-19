package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import com.soywiz.korio.serialization.json.Json

data class LifeCycle(
    var healthCounter: Int = 100
) : Component<LifeCycle>, Json.CustomSerializer {
    override fun type(): ComponentType<LifeCycle> = LifeCycle
    companion object : ComponentType<LifeCycle>()

    override fun encodeToJson(b: StringBuilder) { b.append(this) }
}
