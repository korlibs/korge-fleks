package com.soywiz.korgeFleks.components

import com.github.quillraven.fleks.world
import korlibs.korge.fleks.components.AssetReload
import korlibs.korge.fleks.components.Info
import kotlin.test.Test
import kotlin.test.assertEquals

internal class DebugInfoTest {

    private val expectedWorld = world {}
    private val recreatedWorld = world {}

    @Test
    fun testDebugInfoSerialization() {

        val info = Info(
            configName = "DebugTest",
            showPivotPoint = true
        )
        val assetReload = AssetReload(
            trigger = true
        )

        val entity = expectedWorld.entity {
            it += info
            it += assetReload
        }

        CommonTestEnv.serializeDeserialize(expectedWorld, recreatedWorld)

        // get the component from entity with the same id from the new created world
        val newInfo = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][Info] }
        val newAssetReload = with (recreatedWorld) { recreatedWorld.asEntityBag()[entity.id][AssetReload] }

        assertEquals(info.configName, newInfo.configName, "Check 'name' property to be equal")
        assertEquals(info.showPivotPoint, newInfo.showPivotPoint, "Check 'showPivotPoint' property to be equal")
        assertEquals(assetReload.trigger, newAssetReload.trigger, "Check 'trigger' property to be equal")
    }
}
