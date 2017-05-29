package straightway.general.units

class ElectricCurrent internal constructor(scale: UnitScale): QuantityBase("A", scale, { ElectricCurrent(it) })

val ampere = ElectricCurrent(uni)