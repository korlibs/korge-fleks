package korlibs.korge.fleks.logic.collision.checker

class PlatformerGroundChecker {
}

class SimpleGroundChecker : GroundChecker() {
    override fun onGround(velocityY: Float, cx: Int, cy: Int, xr: Float, yr: Float, collisionChecker: CollisionChecker): Boolean {
        return cy == 750 // && yr <= 0.3f

        // TODO: here we can check the middle point under the player if it is inside a collision box

        // Debug position for ground checking below player start position
        // 1758 - 1024 = 734 + 16 = 750
    }
}