package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.*
import com.soywiz.korge.view.*
import com.soywiz.korgeFleks.utils.KorgeViewCache

data class DebugInfo(
    var name: String = "",
    var showName: Boolean = false,
    var showPivotPoint: Boolean = true,
    var showSizeBox: Boolean = true,
) : Component<DebugInfo> {
    override fun type(): ComponentType<DebugInfo> = DebugInfo

    companion object : ComponentType<DebugInfo>() {
        val onComponentAdded: ComponentHook<DebugInfo> = { entity, component ->
            val korgeDebugViewCache: KorgeViewCache = inject("debugViewCache")
            val debugLayer = inject<HashMap<String, Container>>()["debug_layer"] ?: error("KorgeViewSystem: Cannot find 'debug_layer' in drawingLayers map!")
            val view = Container()

            korgeDebugViewCache.addOrUpdate(entity, view)
            debugLayer.addChild(view)
        }

        val onComponentRemoved: ComponentHook<DebugInfo> = { entity, component ->
            inject<KorgeViewCache>("debugViewCache").getOrNull(entity)?.removeFromParent()
        }
    }
}

class AssetReload(
    var trigger: Boolean = false
) : Component<AssetReload> {
    override fun type(): ComponentType<AssetReload> = AssetReload
    companion object : ComponentType<AssetReload>()
}
