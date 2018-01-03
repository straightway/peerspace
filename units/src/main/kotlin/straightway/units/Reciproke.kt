package straightway.units

import straightway.numbers.div
import straightway.numbers.times

/**
 * The reciproke of a given quantity.
 * Please notice that due to language restrictions, the reciproke of the reciproke,
 * which should end up in the identical unit, is not identical with this type.
 * However, equivalent quantities are guaranteed to have the same id.
 */
class Reciproke<TBaseQuantity : Quantity>
private constructor(
    val wrapped: TBaseQuantity,
    override val scale: UnitScale,
    internal val explicitSymbol: String?,
    private val isAutoScaled: Boolean)
    : Quantity
{
    constructor(wrapped: TBaseQuantity)
        : this(wrapped, wrapped.siScale.reciproke, explicitSymbol = null, isAutoScaled = true)

    override val id: QuantityId
        get() =
            "${one.id}/${wrapped.id}"
    override val baseMagnitude: Number get() =
        1.0 / (wrapped.baseMagnitude * if (explicitSymbol == null) 1 else wrapped.siScale.magnitude)

    override fun toString() =
        if (explicitSymbol == null)
            if (isAutoScaled) "1/$wrapped" else "$scale(1/$wrapped)"
        else
            "$scale$explicitSymbol"

    override fun withScale(scale: UnitScale) =
        Reciproke(wrapped, scale, explicitSymbol, isAutoScaled = false)

    override fun equals(other: Any?) =
        other is Reciproke<*> &&
            wrapped.id == other.wrapped.id &&
            scale == other.scale

    override fun hashCode() =
        wrapped.hashCode() xor scale.hashCode() xor this::class.hashCode()

    infix fun withSymbol(id: String) =
        Reciproke(wrapped, scale, explicitSymbol = id, isAutoScaled = true)
}
