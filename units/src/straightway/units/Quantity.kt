package straightway.units

typealias QuantityId = String

interface Quantity {
    val id: QuantityId
    val scale: UnitScale
    val siScaleCorrection: UnitScale get() = uni
    val valueShift: Number get() = 0
    val baseMagnitude: Number get() = 1
    infix fun withScale(scale: UnitScale): Quantity
}

val Quantity.siScale get() = scale * siScaleCorrection.reciproke
operator fun <Q: Quantity> Q.times(other: One) = this.timesScaleOf(other)
@Suppress("UNCHECKED_CAST")
fun <Q : Quantity> Q.timesScaleOf(other: Quantity) = when (other.siScale) {
    uni -> this
    else -> this.withScale(scale * other.scale * other.siScaleCorrection.reciproke) as Q
}

@Suppress("UNCHECKED_CAST")
fun <Q : Quantity> Q.divScaleOf(other: Quantity) = when (other.siScale) {
    uni -> this
    else -> this.withScale(scale * other.siScaleCorrection * other.scale.reciproke) as Q
}
