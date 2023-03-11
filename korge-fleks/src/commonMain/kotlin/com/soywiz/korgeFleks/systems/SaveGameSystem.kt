package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.soywiz.korgeFleks.components.Sound
import com.soywiz.korgeFleks.utils.SnapshotSerializer

class SaveGameSystem(
    private val serializer: SnapshotSerializer = World.inject()
) : IteratingSystem(
    family { all(Sound) },
    interval = Fixed(step = 0.5f)
) {
    override fun onTickEntity(entity: Entity) {

    }
}


