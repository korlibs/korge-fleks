package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.Family
import com.github.quillraven.fleks.FamilyHook
import com.github.quillraven.fleks.World
import korlibs.korge.fleks.components.Motion
import korlibs.korge.fleks.components.Parallax
import korlibs.korge.fleks.utils.KorgeViewCache

fun parallaxFamily(): Family = World.family { all(Parallax).any(Parallax, Motion) }

val onParallaxFamilyAdded: FamilyHook = { entity ->
    val world = this
    val korgeViewCache = inject<KorgeViewCache>("normalViewCache")

}

val onParallaxFamilyRemoved: FamilyHook = { entity ->
}