package korlibs.korge.fleks.utils

import com.charleskorn.kaml.*
import com.github.quillraven.fleks.*
import korlibs.korge.fleks.entity.config.*
import kotlinx.serialization.modules.*


/**
 * This interface maps the string [name] to a specific game object configuration process.
 * The game object entity will be created by the [entityConfigure] function which can be configured through additional
 * config properties in the derived class.
 *
 * Hint:
 * Deriving the configuration for an entity from this interface keeps the configuration details (config properties)
 * together with the creation/configuration process of a complex game object which can consist of multiple entities.
 * Also, the creation process of the game object can involve multiple steps of [entityConfigure] calls. Thus,
 * it is possible to use a "layered" creation process for entities where each layer adds global, common or more
 * specific components or sub-entities to a game object.
 */
interface EntityConfig {
    val name: String
    fun World.entityConfigure(entity: Entity) : Entity
}

class EntityConfigSerializer {
    private val modulesMap = mutableMapOf<String, SerializersModule>()
    private lateinit var yaml: Yaml
    private var dirty = true

    fun register(name: String, module: SerializersModule) {
        modulesMap[name] = module
        dirty = true
    }

    fun unregister(name: String) {
        modulesMap.remove(name)
    }

    /**
     * Get the Yaml serializer with all registered modules for deserializing entity configs from LDtk levels.
     */
    fun yaml() : Yaml {
        if (dirty) {
            var modules = internalModule
            modulesMap.values.forEach { module ->
                modules = modules.plus(module)
            }
            yaml = Yaml(
                serializersModule = modules,
                configuration = YamlConfiguration(
                    encodingIndentationSize = 4,
                    singleLineStringStyle = SingleLineStringStyle.DoubleQuoted,
                    multiLineStringStyle = MultiLineStringStyle.DoubleQuoted,
                    allowAnchorsAndAliases = true,
                    polymorphismStyle = PolymorphismStyle.Property,
                    polymorphismPropertyName = "entityConfig"
                )
            )
            dirty = false
        }
        return yaml
    }

    /**
     * This polymorphic module config for kotlinx serialization lists all Korge-fleks
     * internal components as subclasses.
     */
    private val internalModule = SerializersModule {
        // Register common entity config classes
        polymorphic(EntityConfig::class) {
            subclass(DialogBoxConfig::class)
            subclass(RichTextConfig::class)
            subclass(ParallaxEffectConfig::class)
            subclass(LogoEntityConfig::class)
            subclass(LevelMapConfig::class)
            subclass(MovedSpawnerObjectConfig::class)
            subclass(CameraConfig::class)
        }
    }
}
