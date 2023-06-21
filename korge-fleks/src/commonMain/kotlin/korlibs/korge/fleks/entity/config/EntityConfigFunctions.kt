package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.Entity


// The nullEntity should be the first created entity in a world. It is used to initialize entity properties of components.
// This entity does not have any components per definition.
val nullEntity: Entity = Entity(id = 0)

inline fun Entity.isNullEntity() : Boolean = this.id == 0
