package straightway.general.units

import straightway.general.numbers.times

data class Time internal constructor(
    override val shortId: String,
    override val scale: UnitScale,
    private val baseMagnitude: Number) : RescalableQuantity
{
    internal constructor(shortId: String, numberOfSeconds: Number) :
        this(shortId, UnitScale("", numberOfSeconds), numberOfSeconds)

    val numberOfSeconds = scale.magnitude

    override fun withScale(scale: UnitScale) =
        Time(shortId, UnitScale(scale.prefix, scale.magnitude * baseMagnitude), baseMagnitude)
    override fun toString() = "$scale$shortId"
}

val second = Time("s", 1)
val minute = Time("min", 60)
val hour = Time("h", 60 * minute.numberOfSeconds.toInt())
val day = Time("d", 24 * hour.numberOfSeconds.toInt())
val week = Time("wk", 7 * day.numberOfSeconds.toInt())
val year = Time("a", 31558432.5504)