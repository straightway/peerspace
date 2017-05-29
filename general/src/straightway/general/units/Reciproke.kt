package straightway.general.units

import straightway.general.numbers.div

data class Reciproke<TBaseQuantity: Quantity, out TWrapped: BaseQuantityProduct<TBaseQuantity>>(
    val wrapped: TWrapped) : QuantityExpr<TBaseQuantity>
{
    override val scale = UnitScale(1.0 / wrapped.scale.magnitude)
    override val exponent by lazy { -wrapped.exponent }
    override val shortId get() = "1/${wrapped.shortId}"
    override fun toString() = "1/$wrapped"
}

fun <TBaseQuantity: Quantity, TWrapped: Factor<TBaseQuantity, TRest>, TRest: QuantityExpr<TBaseQuantity>>
    reciproke(wrapped: TWrapped) = Reciproke(wrapped)