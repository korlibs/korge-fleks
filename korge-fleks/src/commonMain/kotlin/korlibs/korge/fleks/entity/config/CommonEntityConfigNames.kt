package korlibs.korge.fleks.entity.config

import com.github.quillraven.fleks.World
import korlibs.korge.fleks.utils.createAndConfigureEntity


// Common
const val commonMainCamera = "common_main_camera"
const val commonMessagePassingSystem = "common_message_passing_system"
const val commonAttachCameraToEntity = "common_attach_camera"
const val commonWorldMap = "common_world_map"

fun registerCommonEntityConfigs() {
    AttachCameraToEntityConfig(name = commonAttachCameraToEntity)
    MainCameraConfig(name = commonMainCamera)
    MessagePassingSystemConfig(name = commonMessagePassingSystem)
    WorldMapConfig(name = commonWorldMap)
}

fun World.createCommonEntities() {
    // Then create entities from entity config
    createAndConfigureEntity(entityConfig = commonMainCamera)
    createAndConfigureEntity(entityConfig = commonMessagePassingSystem)
    createAndConfigureEntity(entityConfig = commonWorldMap)
}
