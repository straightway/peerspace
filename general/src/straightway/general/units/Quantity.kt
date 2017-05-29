package straightway.general.units

interface Quantity {
    val shortId: String
    val scale: UnitScale
    val siScaleCorrection: UnitScale get() = uni
}

val Quantity.siScale get() = scale * siScaleCorrection.reciproke