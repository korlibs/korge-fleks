package korlibs.korge.fleks.entity.blueprints

import com.github.quillraven.fleks.World
import korlibs.korge.fleks.utils.createAndConfigureEntity


// Common
const val commonMainCamera = "common_main_camera"
const val commonMessagePassingSystem = "common_message_passing_system"
const val commonAttachCameraToEntity = "common_attach_camera"
const val commonWorldMap = "common_world_map"

fun registerCommonEntityBlueprints() {
    AttachCameraToEntityBlueprint(name = commonAttachCameraToEntity)
    MainCameraBlueprint(name = commonMainCamera)
    MessagePassingSystemBlueprint(name = commonMessagePassingSystem)
    WorldMapBlueprint(name = commonWorldMap)
}

fun World.createCommonEntities() {
    // Then create entities from entity config
    createAndConfigureEntity(entityBlueprint = commonMainCamera)
    createAndConfigureEntity(entityBlueprint = commonMessagePassingSystem)
    createAndConfigureEntity(entityBlueprint = commonWorldMap)
}
