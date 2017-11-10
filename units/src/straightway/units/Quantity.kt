package straightway.units

typealias QuantityId = String

/**
 * A quantity which can be attached to a number, forming a unit value. Instances
 * of quantities are units.
 */
interface Quantity {
    /**
     * The identifier of the quantity. All "compatible" quantities have the same id.
     */
    val id: QuantityId

    /**
     * The scale of the quantity's unit.
     */
    val scale: UnitScale

    /**
     * The correction of the scale of the base SI unit.
     * Some units do not have 'uni' as base scale. This is especially true for masses
     * with the SI unit kilogramm.
     */
    val siScaleCorrection: UnitScale get() = uni

    /**
     * Shift of the unit value compared to the default SI unit.
     * Examples: Celsius, Fahrenheit
     */
    val valueShift: Number get() = 0

    /**
     * Define a factor for the magnitude of the unit.
     * E.g. for the non metric units (pounds, feet, etc.).
     */
    val baseMagnitude: Number get() = 1

    /**
     * Get a new unit with a different scale.
     */
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
