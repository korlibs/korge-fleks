package samples.fleks.entities

import com.github.quillraven.fleks.World
import samples.fleks.components.*
import samples.fleks.utils.random

fun World.createExplosionArtefact(position: Position, destruct: Destruct) {
    entity {
        // set initial position of explosion object to collision position
        var xx = position.x
        var yy = position.y - (destruct.explosionParticleRange * 0.5)
        if (destruct.explosionParticleRange != 0.0) {
            xx += (-destruct.explosionParticleRange..destruct.explosionParticleRange).random()
            yy += (-destruct.explosionParticleRange..destruct.explosionParticleRange).random()
        }
        // make sure that all spawned objects are above 200 - this is hardcoded for now since we only have some basic collision detection at y > 200
        // otherwise the explosion artefacts will be destroyed immediately and appear at position 0x0 for one frame
        if (yy > 200.0) { yy = 199.0 }

        it += Position(  // Position of explosion object
            // set initial position of explosion object to collision position
            x = xx,
            y = yy,
            xAcceleration = position.xAcceleration + random(destruct.explosionParticleAcceleration),
            yAcceleration = -position.yAcceleration + random(destruct.explosionParticleAcceleration)
        )
        it += Sprite(
            imageData = "meteorite",  // "" - Disable sprite graphic for spawned object
            animation = "FireTrail",  // "FireTrail" - "TestNum"
            isPlaying = true
        )
        it += Rigidbody(
            mass = 2.0
        )
        it += Impulse()
    }
}
