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

data class Product<QLeft: Quantity, QRight: Quantity>
    private constructor(
        internal val left: QLeft,
        internal val right: QRight,
        override val scale: UnitScale,
        private val isAutoScale: Boolean,
        private val explicitShortId: String? = null)
    : Quantity
{
    constructor(left: QLeft, right: QRight)
        : this(left, right, left.siScale * right.siScale, isAutoScale = true)

    override val shortId: String get() =
    explicitShortId ?: (listOf(shortIdFactors.numerators) + shortIdFactors.denominators).joinToString("/")

    override fun withScale(scale: UnitScale) =
        Product(left, right, scale, isAutoScale = false)

    fun withShortId(newShortId: String) =
        Product(left, right, scale, isAutoScale = false, explicitShortId = newShortId)

    override fun toString() = when {
        isAutoScale && hasUniformRepresentation
            -> (listOf(toStringFactors.numerators) + toStringFactors.denominators).joinToString("/")
        scale == uni -> shortId
        else -> "$scale($shortId)"
    }

    //region private

    private val shortIdFactors by lazy { getFactorRepresentation { shortId } }
    private val toStringFactors by lazy { getFactorRepresentation { toString() } }
    private val hasUniformRepresentation by lazy { toStringFactors.size == shortIdFactors.size }
    private fun getFactorRepresentation(getter: Quantity.() -> String): List<String> {
        return shortIdsOfFactors(getter)
            .sorted()
            .groupBy { it }
            .map { it.key pow it.value.size }
    }

    //endregion
}

operator fun <QLeft: Quantity, QRight: Quantity> QLeft.times(right: QRight) =
    Product(this, right)

operator fun <QLeft: Quantity, QRight: Quantity> QLeft.div(right: QRight) =
    Product(this, reciproke(right))

private infix fun String.pow(exponent: Int) =
    when (exponent) {
        1 -> this
        2 -> "$this²"
        3 -> "$this³"
        else -> "$this^$exponent"
    }

private val List<String>.numerators get() =
    this.filter { !it.startsWith("1/") }.combineWithDefault("1")

private val List<String>.denominators: List<String> get() {
    val result = this.filter { it.startsWith("1/") }.map { it.substring(2) }
    return if (result.isEmpty()) listOf<String>() else listOf(result.joinToString("*"))
}

private fun List<String>.combineWithDefault(default: String) =
    if (this.isEmpty()) default else this.joinToString("*")

private fun Quantity.shortIdsOfFactors(getter: Quantity.() -> String): List<String> = when (this) {
    is Product<*, *> -> left.shortIdsOfFactors(getter) + right.shortIdsOfFactors(getter)
    else -> kotlin.collections.listOf(getter())
}