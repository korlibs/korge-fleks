package korlibs.korge.fleks.utils

import korlibs.korge.fleks.entity.EntityBlueprint
import korlibs.korge.fleks.entity.blueprints.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*


/**
 * This class manages the JSON serializer for deserializing entity configs from LDtk levels. It allows to register
 * additional serializers modules for custom entity configs and provides a JSON serializer with all registered
 * modules.
 */
class EntityConfigSerializer {
    private val modulesMap = mutableMapOf<String, SerializersModule>()
    private lateinit var json: Json
    private var dirty = true

    fun register(name: String, module: SerializersModule) {
        modulesMap[name] = module
        dirty = true
    }

    fun unregister(name: String) {
        modulesMap.remove(name)
    }

    /**
     * Get the JSON serializer with all registered modules for deserializing entity configs from LDtk levels.
     */
    fun json() : Json {
        if (dirty) {
            var modules = internalModule
            modulesMap.values.forEach { module ->
                modules = modules.plus(module)
            }
            json = Json {
                serializersModule = modules
                prettyPrint = true
                encodeDefaults = true
                classDiscriminator = "type"
            }
            dirty = false
        }
        return json
    }

    /**
     * This polymorphic module config for kotlinx serialization lists all Korge-fleks
     * internal components as subclasses.
     */
    private val internalModule = SerializersModule {
        // Register common entity config classes
        polymorphic(EntityBlueprint::class) {
            subclass(DialogBoxBlueprint::class)
            subclass(RichTextBlueprint::class)
            subclass(ParallaxEffectBlueprint::class)
            subclass(LayeredSpriteBlueprint::class)
            subclass(WorldMapBlueprint::class)
            subclass(MovedSpawnerObjectBlueprint::class)
            subclass(MainCameraBlueprint::class)
        }
    }
}
