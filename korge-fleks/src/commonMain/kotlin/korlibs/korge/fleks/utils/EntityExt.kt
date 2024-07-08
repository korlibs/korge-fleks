package korlibs.korge.fleks.utils

import com.github.quillraven.fleks.*


/**
 * Clone function for [Entity] objects. Just for naming consistency.
 */
fun Entity.clone() : Entity = Entity(id, version)
