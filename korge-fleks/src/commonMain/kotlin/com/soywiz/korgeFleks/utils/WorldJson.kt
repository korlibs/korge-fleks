package com.soywiz.korgeFleks.utils

import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import kotlinx.coroutines.sync.Mutex
//import com.soywiz.korio
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.encodeToString
//import kotlinx.serialization.json.Json

object WorldJsonSerializer {
    private val mutex = Mutex()
//    private val jsonParser = Json {  prettyPrint = true }
    private const val prettyPrint = true

    suspend fun worldToJson(world: World) {
        //println(Json.stringify(world.snapshot(), prettyPrint))
//        println(jsonParser.encodeToString(world.snapshot()))
    }

    fun entityToJson(world: World, entity: Entity) {
//        val compactJson = Json.stringify(world.snapshotOf(entity))
//        println("\ncompact:\n\n$compactJson")
//        val json = Json.parse(compactJson)
//        val prettyJson = Json.stringify(json, pretty = true)
//        println("\npretty:\n\n$prettyJson")

//        println(jsonParser.encodeToString(world.snapshotOf(entity)))
//        Json.parse()
    }
}