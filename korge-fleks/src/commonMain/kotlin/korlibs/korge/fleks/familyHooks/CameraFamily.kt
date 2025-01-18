package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*

/**
 * Not needed because view port size is coming from entity config.
 * Kept here for reference.
 */
val mainCameraFamily = World.family { all(SizeComponent, SizeIntComponent, MainCameraTag) }

val onMainCameraAdded: FamilyHook = { entity ->
    // Set view port half which is the middle point of the view port
    val viewPortSize = entity[SizeIntComponent]
    val viewPortHalf = entity[SizeComponent]
    viewPortHalf.width = viewPortSize.width * 0.5f
    viewPortHalf.height = viewPortSize.height * 0.5f

    println("Set view port half: $viewPortHalf")
}
