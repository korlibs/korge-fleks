package com.soywiz.korgeFleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import com.soywiz.korgeFleks.components.*
import com.soywiz.korgeFleks.utils.KorgeViewCache

fun parallaxFamily(): Family = World.family { all(Parallax).any(Parallax, Motion) }

val onParallaxFamilyAdded: FamilyHook = { entity ->
    val world = this
    val korgeViewCache = inject<KorgeViewCache>("normalViewCache")

}

val onParallaxFamilyRemoved: FamilyHook = { entity ->
}