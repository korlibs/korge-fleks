package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.*
import korlibs.korge.fleks.tags.*

/**
 * Not needed because view port size is coming from entity config.
 * Kept here for reference.
 */
val mainCameraFamily = World.family { all(MainCameraTag, PositionComponent) }

val onMainCameraAdded: FamilyHook = { entity ->
}
