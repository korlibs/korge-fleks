package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.world
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DebugInfoTest {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testDebugInfoSerialization() {

        val debugInfo = DebugInfo(
            name = "DebugTest",
            showPivotPoint = true
        )
        val assetReload = AssetReload(
            trigger = true
        )

        val entity = expectedWorld.entity {
            it += debugInfo
            it += assetReload
        }

        serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newDebugInfo = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][DebugInfo] }
        val newAssetReload = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][AssetReload] }

        assertEquals(debugInfo.name, newDebugInfo.name, "Check 'name' property to be equal")
        assertEquals(debugInfo.showPivotPoint, newDebugInfo.showPivotPoint, "Check 'showPivotPoint' property to be equal")
        assertEquals(assetReload.trigger, newAssetReload.trigger, "Check 'trigger' property to be equal")
    }
}
