package straightway.general.units

class One internal constructor(scale: UnitScale)
    : QuantityBase("1", scale, { One(it) })
{
    operator fun <Q: Quantity>times(q: Q) = q.timesScaleOf(this)
    operator fun <Q: Quantity>div(q: Q) = Reciproke(q.divScaleOf(this))
}

val one = One(uni)