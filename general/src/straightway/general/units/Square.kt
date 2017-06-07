package straightway.general.units

typealias Square<T> = Product<T, T>
fun <T: Quantity> square(q: T) = Square(q, q)