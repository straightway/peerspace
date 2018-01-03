package straightway.numbers

import straightway.error.Panic
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.reflect.KClass

data class NumberInfo(
    val prio: Int,
    val unify: Number.() -> Number,
    val round: Number.() -> Number,
    val plus: Number.(Number) -> Number,
    val minus: Number.(Number) -> Number,
    val times: Number.(Number) -> Number,
    val div: Number.(Number) -> Number,
    val rem: Number.(Number) -> Number,
    val compare: Number.(Number) -> Int,
    val min: Number? = null,
    val max: Number? = null) {

    private inline val hasMinMax
        get() = min != null && max != null

    private fun unifyIfPossible(num: Number): Number =
        if (hasMinMax && isInMinMaxRange(num)) unify(num) else num

    private fun isInMinMaxRange(value: Number) =
        (!hasMinMax) || min!! <= value && value <= max!!

    companion object {
        operator fun get(n: Number): NumberInfo = this[n::class]

        operator fun get(type: KClass<*>): NumberInfo =
            _types[type] ?: throw Panic("Unsupported number type: $type")

        @Suppress("UNCHECKED_CAST")
        private fun <T: Number> op(op: T.(T) -> Number) : Number.(Number) -> Number =
            {
                val a = this as T
                val b = it as T
                val result = a.op(b)
                get(a).unifyIfPossible(result)
            }

        private var _types = mapOf(
            Pair(Byte::class, NumberInfo(
                prio = 0,
                min = Byte.MIN_VALUE,
                max = Byte.MAX_VALUE,
                unify = { toByte() },
                round = { this },
                plus = op<Byte>(Byte::plus),
                minus = op<Byte>(Byte::minus),
                times = op<Byte>(Byte::times),
                div = op<Byte>(Byte::div),
                rem = op<Byte>(Byte::rem),
                compare = { (this as Byte).compareTo(it as Byte) })),
            Pair(Short::class, NumberInfo(
                prio = 100,
                min = Short.MIN_VALUE,
                max = Short.MAX_VALUE,
                unify = { toShort() },
                round = { this },
                plus = op<Short>(Short::plus),
                minus = op<Short>(Short::minus),
                times = op<Short>(Short::times),
                div = op<Short>(Short::div),
                rem = op<Short>(Short::rem),
                compare = { (this as Short).compareTo(it as Short) })),
            Pair(Int::class, NumberInfo(
                prio = 200,
                min = Int.MIN_VALUE,
                max = Int.MAX_VALUE,
                unify = { toInt() },
                round = { this },
                plus = op<Int> { this.toLong() + it.toLong() },
                minus = op<Int> { this.toLong() - it.toLong() },
                times = op<Int> { this.toLong() * it.toLong() },
                div = op<Int> { this.toLong() / it.toLong() },
                rem = op<Int>(Int::rem),
                compare = { (this as Int).compareTo(it as Int) })),
            Pair(Long::class, NumberInfo(
                prio = 300,
                min = Long.MIN_VALUE,
                max = Long.MAX_VALUE,
                unify = { toLong() },
                round = { this },
                plus = op<Long> { BigInteger.valueOf(this) + BigInteger.valueOf(it) },
                minus = op<Long> { BigInteger.valueOf(this) - BigInteger.valueOf(it) },
                times = op<Long> { BigInteger.valueOf(this) * BigInteger.valueOf(it) },
                div = op<Long> { BigInteger.valueOf(this) / BigInteger.valueOf(it) },
                rem = op<Long>(Long::rem),
                compare = { (this as Long).compareTo(it as Long) })),
            Pair(BigInteger::class, NumberInfo(
                prio = 400,
                unify = { BigInteger(toString()) },
                round = { this },
                plus = op<BigInteger> { this.add(it) },
                minus = op<BigInteger> { this.subtract(it) },
                times = op(BigInteger::multiply),
                div = op(BigInteger::divide),
                rem = op(BigInteger::remainder),
                compare = { (this as BigInteger).compareTo(it as BigInteger) })),
            Pair(Float::class, NumberInfo(
                prio = 500,
                unify = { toFloat() },
                round = { Math.rint(this.toDouble()).toFloat() },
                plus = op<Float>(Float::plus),
                minus = op<Float>(Float::minus),
                times = op<Float>(Float::times),
                div = op<Float>(Float::div),
                rem = op<Float>(Float::rem),
                compare = { (this as Float).compareTo(it as Float) })),
            Pair(Double::class, NumberInfo(
                prio = 600,
                unify = { toDouble() },
                round = { Math.rint(this.toDouble()) },
                plus = op<Double>(Double::plus),
                minus = op<Double>(Double::minus),
                times = op<Double>(Double::times),
                div = op<Double>(Double::div),
                rem = op<Double>(Double::rem),
                compare = { (this as Double).compareTo(it as Double) })),
            Pair(BigDecimal::class, NumberInfo(
                prio = 700,
                unify = { BigDecimal(toString()) },
                round = { (this as BigDecimal).setScale(0, RoundingMode.HALF_UP) },
                plus = op<BigDecimal> { this.add(it) },
                minus = op<BigDecimal> { this.subtract(it) },
                times = op<BigDecimal> { this.multiply(it) },
                div = op<BigDecimal> {
                    try {
                        this.divide(it)
                    } catch (_: ArithmeticException) {
                        this.divide(it, Math.max(32, Math.max(scale(), it.scale())), RoundingMode.HALF_UP)
                    }
                },
                rem = op<BigDecimal> { this.remainder(it) },
                compare = { (this as BigDecimal).compareTo(it as BigDecimal) })))
    }
}
