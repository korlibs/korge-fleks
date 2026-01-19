package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.components.Position.Companion.PositionComponent
import korlibs.korge.fleks.prefab.SystemRuntimeConfigs
import korlibs.korge.fleks.tags.*

/**
 *
 */
val mainCameraFamily = World.family { all(MainCameraTag, PositionComponent) }

val onMainCameraAdded: FamilyHook = { entity ->
    val systemRuntimeConfigs = inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    systemRuntimeConfigs.camera = entity
}

val onMainCameraRemoved: FamilyHook = { _ ->
    val systemRuntimeConfigs = inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    systemRuntimeConfigs.camera = null
}
