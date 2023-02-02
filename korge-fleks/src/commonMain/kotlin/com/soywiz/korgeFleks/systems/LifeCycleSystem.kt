package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.EachFrame
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.World.Companion.family
import com.soywiz.korgeFleks.components.LifeCycle

class LifeCycleSystem : IteratingSystem(
    family { all(LifeCycle) },
    interval = EachFrame
) {
    override fun onTickEntity(entity: Entity) {
        val lifeCycle = entity[LifeCycle]

        if (lifeCycle.healthCounter <= 0) {
            world -= entity
//            println("LifeCycleSystem: Remove entity '${entity.id}'")
        }
    }
}
