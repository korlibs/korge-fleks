package korlibs.korge.fleks.components.data.tweenSequence


/**
 *  Following component classes are for triggering tweens on specific properties of components
 */
@Serializable @SerialName("TweenTextField")
data class TweenTextField(
    val text: String? = null,
    val textRangeStart: Int? = null,
    val textRangeEnd: Int? = null,

    override var entity: Entity,
    override var delay: Float? = null,
    override var duration: Float? = null,
    @Serializable(with = EasingAsString::class) override var easing: Easing? = null
) : TweenBase {
    override fun clone(): TweenTextField =
        this.copy(
            entity = entity.clone(),
            easing = easing
        )
}
