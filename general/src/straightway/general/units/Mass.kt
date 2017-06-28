package straightway.general.units

import straightway.general.numbers.div
import straightway.general.numbers.times

class Mass constructor(
    scale: UnitScale,
    symbol: String,
    override val siScaleCorrection: UnitScale,
    override val baseMagnitude: Number)
    : QuantityBase(symbol, scale, { Mass(it, symbol, siScaleCorrection, baseMagnitude) })

val gramm = Mass(uni, "g", kilo, 1)
val ton = Mass(uni, "t", uni, 1_000)
val poundAv = Mass(uni, "lb. av.", uni, 0.45359237)
val ounceAv = Mass(uni, "oz. av.", uni, poundAv.baseMagnitude / 16)
val poundAp = Mass(uni, "lb. ap.", uni, poundAv.baseMagnitude * 144 / 175)
val ounceAp = Mass(uni, "oz. ap.", uni, poundAp.baseMagnitude / 12)
val pound = Mass(uni, "lbs", uni, poundAv.baseMagnitude)
val ounce = Mass(uni, "oz", uni, pound.baseMagnitude / 16)
