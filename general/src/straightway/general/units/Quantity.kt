package straightway.general.units

interface Quantity {
    val shortId: String
    val scale: UnitScale
    val siScaleCorrection: UnitScale get() = uni
    fun withScale(scale: UnitScale): Quantity
}

val Quantity.siScale get() = scale * siScaleCorrection.reciproke
operator fun <Q: Quantity> Q.times(other: One) = this.timesScaleOf(other)
fun <Q: Quantity> Q.timesScaleOf(other: Quantity) =
    this.withScale(scale * other.scale * other.siScaleCorrection.reciproke) as Q
fun <Q: Quantity> Q.divScaleOf(other: Quantity) =
    this.withScale(scale * other.siScaleCorrection * other.scale.reciproke) as Q
