package korlibs.korge.fleks.components.data.tweenSequence


@Serializable @SerialName("TweenSwitchLayerVisibility")
data class TweenSwitchLayerVisibility(
    var offVariance: Float? = null,
    var onVariance: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    override fun clone(): TweenSwitchLayerVisibility =
        this.copy(
            entity = entity.clone(),
            easing = easing
        )
}
