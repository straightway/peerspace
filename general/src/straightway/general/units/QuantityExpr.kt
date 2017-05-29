package straightway.general.units

interface QuantityExpr<T: Quantity> : Quantity {
    val exponent: Int
}