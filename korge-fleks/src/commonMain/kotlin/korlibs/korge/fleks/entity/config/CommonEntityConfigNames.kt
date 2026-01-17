package korlibs.korge.fleks.entity.config


// Common
const val commonMainCamera = "common_main_camera"
const val commonMessagePassingSystem = "common_message_passing_system"
const val commonAttachCameraToEntity = "common_attach_camera"

fun registerCommonEntityConfigs() {
    AttachCameraToEntityConfig(name = commonAttachCameraToEntity)
}