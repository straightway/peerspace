package straightway.general.units

data class Reciproke<TBaseQuantity: Quantity>(
    val wrapped: TBaseQuantity, override val scale: UnitScale) : Quantity
{
    constructor(wrapped: TBaseQuantity) : this(wrapped, wrapped.siScale.reciproke)
    override val shortId get() = "1/${wrapped.shortId}"
    override fun toString() = "1/$wrapped"
    override fun withScale(scale: UnitScale) = Reciproke(wrapped, scale)
}

fun <TBaseQuantity: Quantity> reciproke(wrapped: TBaseQuantity) =
    Reciproke(wrapped)