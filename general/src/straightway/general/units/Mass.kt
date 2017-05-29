package straightway.general.units

class Mass internal constructor(scale: UnitScale): QuantityBase("g", scale, { Mass(it) }) {
    override val siScaleCorrection: UnitScale get() = kilo
}

val gramm = Mass(uni)