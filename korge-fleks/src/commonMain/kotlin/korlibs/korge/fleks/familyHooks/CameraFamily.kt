package korlibs.korge.fleks.familyHooks

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.systems.SystemRuntimeConfigs

/**
 *
 */
val onMainCameraAdded: FamilyHook = { entity ->
    val systemRuntimeConfigs = inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    systemRuntimeConfigs.camera = entity
}

val onMainCameraRemoved: FamilyHook = { _ ->
    val systemRuntimeConfigs = inject<SystemRuntimeConfigs>("SystemRuntimeConfigs")
    systemRuntimeConfigs.camera = null
}
