package straightway.general.units

data class Temperature
internal constructor(
    private val symbol: String,
    override val scale: UnitScale,
    private val factor: Number,
    override val valueShift: Number)
    : Quantity {
    override val id: QuantityId by lazy { "${this::class.hashCode()}" }
    override val siScaleCorrection by lazy { UnitScale(factor).reciproke }
    override fun withScale(scale: UnitScale): Quantity = Temperature(symbol, scale, factor, valueShift)
    override fun toString() = "$scale$symbol"
}

val kelvin = Temperature("K", uni, 1, 0)
val celsius = Temperature("°C", uni, 1, 273.15)
val fahrenheit = Temperature("°F", uni, 5.0 / 9.0, 459.67 * 5.0 / 9.0)