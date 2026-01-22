package korlibs.korge.fleks.assets.data


/**
 * Enumeration of different asset types used in the asset management system.
 *
 * Each asset type corresponds to a specific lifetime of the asset within the game:
 * - COMMON: Assets that are shared across multiple worlds or levels.
 * - WORLD: Assets that are specific to a particular world but can be used across different levels within that world.
 * - LEVEL: Assets that are specific to a particular level within a world.
 * - SPECIAL: Assets that are loaded for specific chunks or sections of a level, often used for optimization.
 * - UNKNOWN: Assets that could not be found in the asset store. They are created on the fly and act as placeholders.
 */
enum class AssetType(val folder: String) {
    COMMON("common"),
    WORLD("world"),
    LEVEL("level"),
    SPECIAL("chunk"),
    UNKNOWN("unknown")
}
