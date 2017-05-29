package straightway.general.units

interface BaseQuantityProduct<TBaseQuantity: Quantity> : QuantityExpr<TBaseQuantity>

data class Factor<TBaseQuantity: Quantity, out TRest: QuantityExpr<TBaseQuantity>>
    (val wrapped: TBaseQuantity, val rest: TRest) : BaseQuantityProduct<TBaseQuantity>
{
    override val scale = combinedScale(wrapped, rest)
    override val exponent = 1 + rest.exponent

    override val shortId get() = "$wrappedShortIdBase$exponentSuffix"
    override fun toString() = "$wrappedString$exponentSuffix"

    //region Private
    private val hasExponent = 1 != exponent
    private val isShortIdBaseInParens = hasExponent && wrapped.siScaleCorrection != uni
    private val wrappedShortIdBase = if (isShortIdBaseInParens) "(${wrapped.shortId})" else wrapped.shortId
    private val isStringRepBaseInParens = hasExponent && wrapped.scale != uni
    private val wrappedString = if (isStringRepBaseInParens) "($wrapped)" else "$wrapped"
    private val exponentSuffix = when (exponent) {
        1 -> ""
        2 -> "²"
        3 -> "³"
        else -> "^$exponent"
    }

    private companion object {
        fun combinedScale(wrapped: Quantity, rest: Quantity) =
            wrapped.scale * wrapped.siScaleCorrection.reciproke * rest.scale
    }
    // endregion
}