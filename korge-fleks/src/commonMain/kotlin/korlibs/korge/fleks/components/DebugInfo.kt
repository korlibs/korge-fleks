package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.view.*
import korlibs.korge.fleks.utils.KorgeViewCache
import korlibs.korge.fleks.utils.SerializeBase
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("DebugInfo")
data class DebugInfo(
    var name: String = "",
    var showName: Boolean = false,
    var showPivotPoint: Boolean = true,
    var showSizeBox: Boolean = true,
) : Component<DebugInfo>, SerializeBase {
    override fun type(): ComponentType<DebugInfo> = DebugInfo

    companion object : ComponentType<DebugInfo>() {
        val onComponentAdded: ComponentHook<DebugInfo> = { entity, _ ->
            val korgeDebugViewCache: KorgeViewCache = inject("debugViewCache")
            val debugLayer = inject<HashMap<String, Container>>()["debug_layer"] ?: error("DebugInfo: Cannot find 'debug_layer' in layers map!")
            val view = Container()

            korgeDebugViewCache.addOrUpdate(entity, view)
            debugLayer.addChild(view)
        }

        val onComponentRemoved: ComponentHook<DebugInfo> = { entity, _ ->
            inject<KorgeViewCache>("debugViewCache").getOrNull(entity)?.removeFromParent()
        }
    }
}

@Serializable
@SerialName("AssetReload")
class AssetReload(
    var trigger: Boolean = false
) : Component<AssetReload>, SerializeBase {
    override fun type(): ComponentType<AssetReload> = AssetReload
    companion object : ComponentType<AssetReload>()
}
