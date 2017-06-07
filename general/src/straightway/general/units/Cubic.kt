package straightway.general.units

typealias Cubic<T> = Product<T, Square<T>>
fun <T: Quantity> cubic(q: T) = Cubic(q, square(q))