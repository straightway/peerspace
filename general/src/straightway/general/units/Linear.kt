package straightway.general.units

typealias Linear<T> = Factor<T, None<T>>fun <T: Quantity> linear(q: T) = Linear(q, None(q))