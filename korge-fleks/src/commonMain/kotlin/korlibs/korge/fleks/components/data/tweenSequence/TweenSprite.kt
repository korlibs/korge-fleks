package korlibs.korge.fleks.components.data.tweenSequence


@Serializable @SerialName("TweenSprite")
data class TweenSprite(
    var animation: String? = null,
    // do not tween the frameIndex, it is updated by the SpriteSystem
    var running: Boolean? = null,
    var direction: ImageAnimation.Direction? = null,
    var destroyOnPlayingFinished: Boolean? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    override fun clone(): TweenSprite =
        this.copy(
            direction = direction,
            entity = entity.clone(),
            easing = easing
        )
}
