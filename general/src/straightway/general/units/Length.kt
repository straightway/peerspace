package straightway.general.units

class Length internal constructor(scale: UnitScale): QuantityBase("m", scale, { Length(it) })

val meter = Length(uni)