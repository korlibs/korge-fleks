package com.soywiz.korgeFleks.systems

import com.github.quillraven.fleks.*
import com.github.quillraven.fleks.World.Companion.family
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.assets.AssetStore

/**
 *
 *
 */
class TouchInputSystem(
    private val assets: AssetStore = World.inject()
) : IteratingSystem(
    family {
        all(InputTouchButton)
    },
    interval = EachFrame
) {


    override fun onTickEntity(entity: Entity) {
        val inputTouchButton = entity[InputTouchButton]
//        if (inputTouchButton.onDown) {
//            inputTouchButton.onDown = false
//            println("down")
//        }
//        if (inputTouchButton.onUp) {
//            inputTouchButton.onUp = false
//            println("up")
//        }
    }
}