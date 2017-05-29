package straightway.general.units

data class None<TBaseQuantity: Quantity>(val baseUnit: TBaseQuantity)
    : QuantityExpr<TBaseQuantity>
{
    override val exponent = 0
    override val shortId get() = ""
    override val scale = uni
    override fun toString() = ""
    operator fun times(factor: TBaseQuantity) = linear(factor)
    operator fun div(factor: TBaseQuantity) = Reciproke(linear(factor))
}

val NoN = None(mol)
val NoI = None(ampere)
val NoL = None(meter)
val NoJ = None(candela)
val NoM = None(kilo(gramm))
val NoTheta = None(kelvin)
val NoT = None(second)