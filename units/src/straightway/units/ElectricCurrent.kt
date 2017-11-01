package straightway.units

class ElectricCurrent constructor(scale: UnitScale) : QuantityBase("A", scale, { ElectricCurrent(it) })

val ampere = ElectricCurrent(uni)