package korlibs.korge.fleks.components.data.tweenSequence


@Serializable @SerialName("Wait")
data class Wait(
    override var duration: Float? = Float.MAX_VALUE,  // Use duration by setting explicitly a value
    val event: Int? = null,                       // Wait for a specific event if eventId is not "null" - need to unblock from infinite wait (Float.MAX_VALUE)

    override var entity: Entity = Entity.NONE,    // not used
    override var delay: Float? = null,            // Not used
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null  // not used
) : TweenBase {
    override fun clone(): Wait =
        this.copy(
            entity = entity.clone(),
            easing = easing
        )
}
