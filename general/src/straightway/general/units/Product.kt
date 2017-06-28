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

import straightway.general.Panic
import straightway.general.numbers.times
import java.lang.Integer.max

class Product<QLeft : Quantity, QRight : Quantity>
    private constructor(
        internal val left: QLeft,
        internal val right: QRight,
        override val scale: UnitScale,
        private val isAutoScale: Boolean,
        override val baseMagnitude: Number,
        private val explicitSymbol: String? = null,
        override val siScaleCorrection: UnitScale = uni)
    : Quantity
{
    constructor(left: QLeft, right: QRight)
        : this(left, right, left.siScale * right.siScale, true, left.baseMagnitude * right.baseMagnitude)

    override val id: QuantityId get() =
    (listOf(idFactors.numerators(one.id)) + idFactors.denominators).joinToString("/")

    override infix fun withScale(scale: UnitScale) =
        Product(left, right, scale, false, baseMagnitude, explicitSymbol)

    infix fun withSymbol(newSymbol: String) =
        Product(left, right, uni, false, baseMagnitude, newSymbol, siScale.reciproke)

    infix fun withBaseMagnitude(baseMagnitude: Number): Product<QLeft, QRight> =
        Product(left, right, scale, isAutoScale, baseMagnitude, explicitSymbol)

    @Suppress("UNCHECKED_CAST")
    infix fun <Q : Product<*, *>> normalizedToTypeOf(target: Q): Q = when {
        id != target.id -> throw Panic("Unit $this cannot be normalized as $target")
        explicitSymbol != null -> target withSymbol explicitSymbol
        else -> target
    }.withScale(scale).withBaseMagnitude(baseMagnitude) as Q

    override fun equals(other: Any?) =
        other is Product<*, *> &&
            id == other.id &&
            scale == other.scale

    override fun hashCode() =
        left.hashCode() xor
            right.hashCode() xor
            scale.hashCode() xor
            baseMagnitude.hashCode() xor
            (explicitSymbol?.hashCode() ?: 0)

    override fun toString() = when {
        isAutoScale && hasUniformRepresentation
        -> (listOf(toStringFactors.numerators("1")) + toStringFactors.denominators).joinToString("/")
        scale == uni -> explicitSymbol ?: toStringBase
        else -> if (explicitSymbol == null) "$scale($toStringBase)" else "$scale$explicitSymbol"
    }

    //region private

    private val idFactors by lazy { getFactorRepresentation { id } }
    private val toStringFactors by lazy { getFactorRepresentation { toString() } }
    private val toStringBaseFactors by lazy { getFactorRepresentation { withScale(uni * UnitScale(baseMagnitude)).toString() } }
    private val toStringBase: String get() = (listOf(toStringBaseFactors.numerators("1")) + toStringBaseFactors.denominators).joinToString("/")
    private val hasUniformRepresentation by lazy { toStringFactors.size == idFactors.size }
    private fun getFactorRepresentation(getter: Quantity.() -> String): List<String> {
        return sortedFactors(getter)
            .groupBy { it }
            .cancelled()
            .map { it.key pow it.value }
            .sorted()
    }

    //endregion
}

operator fun <QLeft: Quantity, QRight: Quantity> QLeft.times(right: QRight) =
    Product(this, right)

operator fun <QLeft: Quantity, QRight: Quantity> QLeft.div(right: QRight) =
    Product(this, Reciproke(right))

typealias Square<T> = Product<T, T>
fun <T : Quantity> square(q: T) = Square(q, q)

typealias Cubic<T> = Product<T, Square<T>>
fun <T : Quantity> cubic(q: T) = Cubic(q, square(q))

//region Private

private infix fun String.pow(exponent: Int) =
    when (exponent) {
        1 -> this
        2 -> "$this²"
        3 -> "$this³"
        else -> "$this^$exponent"
    }

private fun List<String>.numerators(default: String) =
    this.filter { !it.id }.combineWithDefault(default)

private val List<String>.denominators: List<String> get() {
    val result = this.filter { it.id }.map { it.substring(2) }
    return if (result.isEmpty()) listOf<String>() else listOf(result.joinToString("*"))
}

private fun List<String>.combineWithDefault(default: String) =
    if (this.isEmpty()) default else this.joinToString("*")

private fun Quantity.sortedFactors(getter: Quantity.() -> String): List<String> = when (this) {
    is Product<*, *> -> left.sortedFactors(getter) + right.sortedFactors(getter)
    is Reciproke<*> -> this.wrapped.sortedFactors(getter).map { it.reciproke }
    else -> kotlin.collections.listOf(getter())
}

private fun Map<String, List<String>>.cancelled() =
    map(this::cancelFactor).filter(::hasNotNullExponent).toMap()

private fun Map<String, List<String>>.cancelFactor(entry: Map.Entry<String, List<String>>) =
    Pair(entry.key, max(0, entry.exponent - entry.reciprokeExponent(this)))

private fun hasNotNullExponent(entry: Pair<String, Int>) = entry.second != 0
private fun Map.Entry<String, List<String>>.reciprokeExponent(factors: Map<String, List<String>>) =
    factors[reciproke]?.size ?: 0

private val Map.Entry<String, List<String>>.exponent get() = value.size
private val Map.Entry<String, List<String>>.reciproke get() = key.reciproke
private val String.reciproke get() = if (this.id) substring(2) else "1/${this}"
private val String.id get() = startsWith("1/")

//endregion