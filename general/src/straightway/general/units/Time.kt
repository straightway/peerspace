package straightway.general.units

import straightway.general.numbers.times

data class Time internal constructor(
    private val symbol: String,
    override val scale: UnitScale,
    private val baseMagnitude: Number) : Quantity
{
    internal constructor(symbol: String, numberOfSeconds: Number) :
        this(symbol, UnitScale("", numberOfSeconds), numberOfSeconds)

    override val shortId = "s"
    val numberOfSeconds = scale.magnitude

    override fun withScale(scale: UnitScale) =
        Time(symbol, UnitScale(scale.prefix, scale.magnitude * baseMagnitude), baseMagnitude)

    override fun toString() = "$scale$symbol"
}

val second = Time("s", 1)
val minute = Time("min", 60)
val hour = Time("h", 60 * minute.numberOfSeconds.toInt())
val day = Time("d", 24 * hour.numberOfSeconds.toInt())
val week = Time("wk", 7 * day.numberOfSeconds.toInt())
val year = Time("a", 31558432.5504)