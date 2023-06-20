package korlibs.korge.fleks.components

import com.github.quillraven.fleks.*
import korlibs.korge.fleks.utils.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable



@Serializable @SerialName("Info")
data class Info(
    var name: String = "noName",
    var showName: Boolean = false,
    var showPivotPoint: Boolean = true,
    var showSizeBox: Boolean = false,
) : Component<Info>, SerializeBase {
    override fun type(): ComponentType<Info> = Info

    companion object : ComponentType<Info>() {
        val onComponentAdded: ComponentHook<Info> = { entity, component ->
            // TODO Implement this in DebugSystem depending if showSizeBox, etc. is set
//            val korgeViewCacheDebug: KorgeViewCache = inject("KorgeViewCacheDebug")
//            val debugLayer = inject<HashMap<String, Container>>("Layers")["debug_layer"] ?: error("DebugInfo: Cannot find 'debug_layer' in layers map!")
//            val view = Container()
//            korgeViewCacheDebug.addOrUpdate(entity, view)
//            debugLayer.addChild(view)
            val entityByName = inject<EntityByName>("EntityByName")
            entityByName.add(component.name, entity)
        }

        val onComponentRemoved: ComponentHook<Info> = { _, component ->
            // TODO
//            inject<KorgeViewCache>("KorgeViewCacheDebug").getOrNull(entity)?.removeFromParent()
            val entityByName = inject<EntityByName>("EntityByName")
            entityByName.remove(component.name)
        }
    }
}
