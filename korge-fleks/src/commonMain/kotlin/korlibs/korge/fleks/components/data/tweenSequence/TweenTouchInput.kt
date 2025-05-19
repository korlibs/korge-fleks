package korlibs.korge.fleks.components.data.tweenSequence


@Serializable @SerialName("TweenTouchInput")
data class TweenTouchInput(
    var enabled: Boolean? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    override fun clone(): TweenTouchInput =
        this.copy(
            entity = entity.clone(),
            easing = easing
        )
}
