package straightway.general.units

import straightway.general.numbers.times

val gravityAccelleration = 9.80665[meter / square(second)]

typealias AbsorbedDose = Product<Square<Length>, Reciproke<Square<Time>>>
val gray: AbsorbedDose = square(meter) / square(second) withSymbol "Gy"

typealias Accelleration = Product<Length, Reciproke<Square<Time>>>

typealias Angle = Product<Length, Reciproke<Length>>
val radian: Angle = meter / meter withSymbol "rad"
val degree: Angle = Length("mPI/180", uni, Math.PI / 180.0) / meter withSymbol "°"

typealias Area = Square<Length>
val are: Area = square(Length("dam", uni, 10)) withSymbol "a"
val hectare: Area = square(Length("hm", uni, 100)) withSymbol "ha"

typealias Bandwidth = Product<AmountOfData, Reciproke<Time>>

typealias CatalyticActivity = Product<AmountOfSubstance, Reciproke<Time>>
val katal = mol / second withSymbol "kat"

typealias Capacitance = Product<Product<Product<Square<Time>, Square<Time>>, Square<ElectricCurrent>>, Reciproke<Product<Mass, Square<Length>>>>
val farad: Capacitance = square(second) * square(second) * square(ampere) / (kilo(gramm) * square(meter)) withSymbol "F"

typealias ElectricCharge = Product<ElectricCurrent, Time>
val coulomb: ElectricCharge = ampere * second withSymbol "C"

typealias ElectricResistance = Product<Product<Mass, Square<Length>>, Reciproke<Product<Cubic<Time>, Square<ElectricCurrent>>>>
val ohm: ElectricResistance = kilo(gramm) * square(meter) / (cubic(second) * square(ampere)) withSymbol "Ω"

typealias ElectricalConsuctance = Product<Product<Cubic<Time>, Square<ElectricCurrent>>, Reciproke<Product<Mass, Square<Length>>>>
val siemens = cubic(second) * square(ampere) / (kilo(gramm) * square(meter)) withSymbol "S"

typealias Energy = Product<Product<Mass, Square<Length>>, Reciproke<Square<Time>>>
val joule: Energy = kilo(gramm) * square(meter) / square(second) withSymbol "J"

typealias EquivalentDose = Product<Square<Length>, Reciproke<Square<Time>>>
val sievert: EquivalentDose = square(meter) / square(second) withSymbol "Sv"

typealias Force = Product<Product<Mass, Length>, Reciproke<Square<Time>>>
val newton: Force = (kilo(gramm) * meter / square(second)) withSymbol "N"
val poundForce: Force = Mass(uni, "lbs", uni, poundAv.baseMagnitude * gravityAccelleration.value) * meter / square(second) withSymbol "lbf"

typealias Frequency = Reciproke<Time>
val hertz: Frequency = Reciproke(second) withSymbol "Hz"

typealias Illuminance = Product<LuminousIntensity, Reciproke<Square<Length>>>
val lux: Illuminance = candela / square(meter) withSymbol "lx"

typealias Inductance = Product<Product<Mass, Square<Length>>, Reciproke<Product<Square<Time>, Square<ElectricCurrent>>>>
val henry: Inductance = kilo(gramm) * square(meter) / (square(second) * square(ampere)) withSymbol "H"

typealias LuminousFlux = Product<LuminousIntensity, Product<Square<Length>, Reciproke<Square<Length>>>>
val lumen: LuminousFlux = (candela * (square(meter) / square(meter))).withSymbol("lm")

typealias MagneticFlux = Product<Product<Mass, Square<Length>>, Reciproke<Product<Square<Time>, ElectricCurrent>>>
val weber: MagneticFlux = kilo(gramm) * square(meter) / (square(second) * ampere) withSymbol "Wb"

typealias MagneticFluxDensity = Product<Mass, Reciproke<Product<Square<Time>, ElectricCurrent>>>
val tesla: MagneticFluxDensity = kilo(gramm) / (square(second) * ampere) withSymbol "T"

typealias Power = Product<Product<Mass, Square<Length>>, Reciproke<Cubic<Time>>>
val watt: Power = kilo(gramm) * square(meter) / cubic(second) withSymbol "W"

typealias Pressure = Product<Mass, Reciproke<Product<Length, Square<Time>>>>
val pascal: Pressure = kilo(gramm) / (meter * square(second)) withSymbol "Pa"
val bar: Pressure = pascal withBaseMagnitude 1e5 withSymbol "bar"
val psi: Pressure = poundForce / square(inch) normalizedToTypeOf pascal withSymbol "psi"

typealias Radioactivity = Reciproke<Time>
val becquerel: Radioactivity = Reciproke(second) withSymbol "Bq"

typealias Speed = Product<Length, Reciproke<Time>>
val knot: Speed = (nauticalMile / hour) withSymbol "knot"

typealias SolidAngle = Product<Square<Length>, Reciproke<Square<Length>>>
val steradian = square(meter) / square(meter) withSymbol "sr"

typealias Voltage = Product<Product<Mass, Square<Length>>, Reciproke<Product<Cubic<Time>, ElectricCurrent>>>
val volt: Voltage = kilo(gramm) * square(meter) / (cubic(second) * ampere) withSymbol "V"

typealias Volume = Cubic<Length>
val liter: Volume = cubic(Length("dm", uni, 0.1)) withSymbol "l"
