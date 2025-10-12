package korlibs.korge.fleks.assets.data.ldtk

import korlibs.datastructure.Extra
import korlibs.io.dynamic.*
import korlibs.io.lang.*
import korlibs.math.geom.Anchor
import korlibs.math.geom.PointInt
import kotlinx.serialization.json.*


val EntityInstance.gridPos: PointInt get() = PointInt(grid[0], grid[1])
val EntityInstance.pixelPos: PointInt get() = PointInt(px[0], px[1])
val EntityInstance.pivotAnchor: Anchor get() = Anchor(pivot[0], pivot[1])
val EntityInstance.fieldInstancesByName: Map<String, FieldInstance> by Extra.PropertyThis { fieldInstances.associateBy { it.identifier } }
val EntityDefinition.fieldDefsByName by Extra.PropertyThis { this.fieldDefs.associateBy { it.identifier } }
val Definitions.entitiesByUid by Extra.PropertyThis { this.entities.associateBy { it.uid } }
operator fun EntityDefinition.get(name: String): FieldDefinition? = fieldDefsByName[name]
fun FieldInstance.definition(ldtk: LDTKJson): FieldDefinition {
    return ldtk.defs.levelFields[this.defUid]
}
data class FieldInfo(val def: FieldDefinition, val instance: FieldInstance) {
    val identifier = def.identifier

    val value: Any? get() = valueJson.toKotlinTypes()
    val valueDyn: Dyn get() = Dyn(value)
    val valueJson: JsonElement? get() = instance.value
    val valueString: String? get() = valueJson?.jsonPrimitive?.content
}

val FieldInstance?.valueDyn: Dyn get() = Dyn(this?.value.toKotlinTypes())

private fun JsonElement?.toKotlinTypes(): Any? = when {
    this == null -> null
    this is JsonNull -> null
    this is JsonArray -> this.toList().map { it.toKotlinTypes() }
    this is JsonObject -> this.toMap().mapValues { it.value.toKotlinTypes() }
    this is JsonPrimitive -> {
        val content = this.content
        when {
            this.isString -> content
            content == "undefined" -> null
            content == "true" -> true
            content == "false" -> false
            else -> content.toDoubleOrNull() ?: Double.NaN
        }
    }
    else -> unexpected("Don't know how to parse $this")
}
