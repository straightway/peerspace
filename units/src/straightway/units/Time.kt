package straightway.units

import straightway.numbers.minus
import straightway.numbers.times
import java.time.Duration
import java.time.LocalDateTime

class Time constructor(
    symbol: String,
    scale: UnitScale,
    baseMagnitude: Number)
    : QuantityBase("s", symbol, scale, baseMagnitude, { Time(symbol, it, baseMagnitude) })
{
    constructor(symbol: String, numberOfSeconds: Number) : this(symbol, uni, numberOfSeconds)
    val numberOfSeconds get() = scale.magnitude * baseMagnitude
}

val second = Time("s", 1)
val minute = Time("min", 60)
val hour = Time("h", 60 * minute.numberOfSeconds.toInt())
val day = Time("d", 24 * hour.numberOfSeconds.toInt())
val week = Time("wk", 7 * day.numberOfSeconds.toInt())
val year = Time("a", 31558432.5504)

fun UnitValue<*, Time>.toDuration(): Duration {
    val seconds = this.baseValue.toLong()
    val nanos = ((this.baseValue - seconds) * 1_000_000_000).toLong()
    return Duration.ofSeconds(seconds, nanos)
}

operator fun <T : Number> LocalDateTime.plus(amount: UnitValue<T, Time>) =
    this + amount.toDuration()

operator fun <T : Number> LocalDateTime.minus(amount: UnitValue<T, Time>) =
    this - amount.toDuration()
