package straightway.general.units

import straightway.general.numbers.div
import straightway.general.numbers.times

data class UnitScale(val prefix: String, val magnitude: Number) {
    private constructor(original: UnitScale) : this(original.prefix, original.magnitude)
    constructor(magnitude: Number) : this(createScaleFor(magnitude))
    val reciproke by lazy { createScaleFor(1.0 / magnitude) }

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Quantity> invoke(unit: T) = unit.withScale(unit.scale * this) as T

    override fun toString() = prefix

    operator fun times(other: UnitScale) = createScaleFor(magnitude * other.magnitude)

    private val keyValuePair get() = Pair(key(magnitude), this)

    companion object {
        fun predefine(prefix: String, magnitude: Number): UnitScale {
            val result = UnitScale(prefix, magnitude)
            synchronized(lock) { magnitudeToScale = magnitudeToScale + Pair(key(magnitude), result) }
            return result
        }

        internal fun createScaleFor(magnitude: Number): UnitScale =
            fixedScaleFor(magnitude) ?: UnitScale.predefine("[$magnitude]", magnitude)

        internal fun fixedScaleFor(magnitude: Number): UnitScale? =
            magnitudeToScale[key(magnitude)]

        private val lock = Any()
        private var magnitudeToScale: Map<String, UnitScale>

        private fun key(magnitude: Number) = magnitude.toString().let {
            if (it.contains('.')) it.trimEnd(' ', '0').trimEnd('.') else it
        }
        
        init {
            magnitudeToScale = mapOf<String, UnitScale>(
                UnitScale("a", 1e-18).keyValuePair,
                UnitScale("f", 1e-15).keyValuePair,
                UnitScale("p", 1e-12).keyValuePair,
                UnitScale("n", 1e-9).keyValuePair,
                UnitScale("µ", 1e-6).keyValuePair,
                UnitScale("m", 1e-3).keyValuePair,
                UnitScale("c", 1e-2).keyValuePair,
                UnitScale("d", 1e-1).keyValuePair,
                UnitScale("", 1).keyValuePair,
                UnitScale("da", 10).keyValuePair,
                UnitScale("h", 100).keyValuePair,
                UnitScale("k", 1_000).keyValuePair,
                UnitScale("M", 1_000_000).keyValuePair,
                UnitScale("G", 1_000_000_000).keyValuePair,
                UnitScale("T", 1_000_000_000_000).keyValuePair,
                UnitScale("P", 1_000_000_000_000_000).keyValuePair,
                UnitScale("E", 1_000_000_000_000_000_000).keyValuePair,
                UnitScale("Ki", 1_024).keyValuePair,
                UnitScale("Mi", 1_048_576).keyValuePair,
                UnitScale("Gi", 1_073_741_824).keyValuePair,
                UnitScale("Ti", 1_099_511_627_776).keyValuePair,
                UnitScale("Pi", 1_125_899_906_842_624).keyValuePair,
                UnitScale("Ei", 1_152_921_504_606_846_976).keyValuePair)
        }
    }
}