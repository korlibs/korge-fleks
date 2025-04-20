package korlibs.korge.fleks.utils.poolableData

import korlibs.korge.fleks.utils.poolableData.PoolableString.Companion.StringPool
import kotlinx.serialization.Serializable
import kotlin.jvm.*


/**
 * A string that can be pooled to avoid garbage collection on frequently
 * creation and deletion of strings with the same content.
 *
 * @param name The string value.
 */
@JvmInline @Serializable
value class PoolableString private constructor(
    val name: String
) {
    companion object {
        val EMPTY = PoolableString("")
        operator fun invoke(name: String): PoolableString =
            StringPool[name]

        /**
         * A pool of strings to avoid creating new string instances when they are already in use.
         */
        object StringPool {
            private val pool: MutableMap<String, PoolableString> = mutableMapOf()

            operator fun get(key: String): PoolableString {
                return pool.getOrPut(key) { PoolableString(key) }
            }
        }

    }

    override fun toString(): String = name
}

fun String.toPoolableString(): PoolableString = StringPool[this]

fun MutableList<PoolableString>.fromStringList(list: List<String>) {
    list.forEach { string -> this.add(StringPool[string]) }
}

fun MutableList<PoolableString>.fromStrings(vararg strings: String) {
    strings.forEach { string -> this.add(StringPool[string]) }
}

fun MutableList<PoolableString>.init(other: MutableList<PoolableString>) {
    this.addAll(other)
}
