package korlibs.korge.fleks.components.data.tweenSequence


@Serializable @SerialName("TweenSpawner")
data class TweenSpawner(
    var numberOfObjects: Int? = null,
    var interval: Int? = null,
    var timeVariation: Int? = null,
    var positionVariation: Float? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    override fun clone(): TweenSpawner =
        this.copy(
            entity = entity.clone(),
            easing = easing
        )
}
