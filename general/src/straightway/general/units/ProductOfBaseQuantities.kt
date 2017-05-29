/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.general.units

typealias EN = QuantityExpr<AmountOfSubstance>
typealias EI = QuantityExpr<ElectricCurrent>
typealias EL = QuantityExpr<Length>
typealias EJ = QuantityExpr<LuminousIntensity>
typealias EM = QuantityExpr<Mass>
typealias ETheta = QuantityExpr<Temperature>
typealias ET = QuantityExpr<Time>

class ProductOfBaseQuantities<
    out N: EN,
    out I: EI,
    out L: EL,
    out J: EJ,
    out M: EM,
    out Theta: ETheta,
    out T: ET>
private constructor(
    val n: N,
    val i: I,
    val l: L,
    val j: J,
    val m: M,
    val theta: Theta,
    val t: T,
    shortId: String,
    scale: UnitScale,
    private val stringRepresentation: String)
    : QuantityBase(
        shortId,
        scale,
        { ProductOfBaseQuantities(
            n, i, l, j, m, theta, t,
            shortId,
            it,
            "$it$shortId") })
{
    private constructor(
        n: N, i: I, l: L, j: J, m: M, theta: Theta, t: T,
        quantities: Array<QuantityExpr<out RescalableQuantity>>)
        : this(n, i, l, j, m, theta, t,
               stringRep(quantities) { shortId },
               computeScale(quantities),
               stringRep(quantities) { toString() })

    constructor(n: N, i: I, l: L, j: J, m: M, theta: Theta, t: T)
        : this(n, i, l, j, m, theta, t, arrayOf(n, i, l, j, m, theta, t))

    override fun toString() = stringRepresentation

    private companion object {
        fun computeScale(quantities: Array<QuantityExpr<out RescalableQuantity>>) =
            quantities.map { it.scale }.reduce { a, b -> a * b }

        fun stringRep(
            quantities: Array<QuantityExpr<out RescalableQuantity>>,
            stringer: QuantityExpr<*>.() -> String): String
        {
            val singleIds = quantities.map { it.stringer() }
            if (singleIds.all { it.isEmpty() }) return ""
            val numeratorIds = singleIds.filter { !(it.isEmpty() || it.startsWith("1/")) }
            val numerator =
                if (numeratorIds.isEmpty()) "1"
                else numeratorIds.joinToString("*")
            val denomitatorIds = singleIds.filter { it.startsWith("1/") }.map { it.substring(2) }
            val denominator = when (denomitatorIds.size) {
                0 -> ""
                1 -> "/${denomitatorIds.single()}"
                else -> "/(${denomitatorIds.joinToString("*")})"
            }
            return "$numerator$denominator"
        }
    }
}

val oneUnitProduct = ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, NoTheta, NoT)