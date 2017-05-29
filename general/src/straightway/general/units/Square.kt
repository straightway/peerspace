package straightway.general.units

typealias Square<T> = Factor<T, Linear<T>>fun <T: Quantity> square(q: T) = Square(q, linear(q))