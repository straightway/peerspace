package straightway.general.units


typealias Force = Product<Product<Mass, Length>, Reciproke<Square<Time>>>
val newton: Force = kilo(gramm) * meter / square(second)
