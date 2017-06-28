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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StandardQuantityTest {

    @Test fun equality() = testedQuantities.forEach { sut = it; testEquality() }
    @Test fun comparison() = testedQuantities.forEach { sut = it; testComparison() }
    @Test fun toString_scaled() = testedQuantities.forEach { sut = it; testToString_scaled() }
    @Test fun toString_unscaled() = testedQuantities.forEach { sut = it; testToString_unscaled() }
    @Test fun times_one() = testedQuantities.forEach { sut = it; testTimesOne() }
    @Test fun id_scaled() = testedQuantities.forEach { sut = it; testId_scaled() }
    @Test fun id_unscaled() = testedQuantities.forEach { sut = it; testId_unscaled() }
    @Test fun uniqueId() = testedQuantities.forEach { sut = it; testUniqueId() }

    private fun testEquality() = assertEquals(1.0[sut.quantity], 100.0[centi(sut.quantity)])
    { "${sut.quantity}.equals" }

    private fun testComparison() = assertTrue(1.0[sut.quantity] > 10.0[centi(sut.quantity)])
    { "${sut.quantity}.compareTo" }

    private fun testToString_unscaled() = assertEquals("1 ${sut.stringRep}".trimEnd(), 1[sut.quantity].toString())
    { "${sut.quantity}.toString" }

    private fun testToString_scaled() = assertEquals(
        if (sut.mustUseParens) "1 m(${sut.stringRep})" else "1 m${sut.stringRep}",
        1[milli(sut.quantity)].toString()) { "${sut.quantity}.toString" }

    private fun testTimesOne() = assertEquals(sut.quantity.timesScaleOf(kilo(one)), sut.quantity * kilo(one))
    { "${sut.quantity}.times(one)" }

    private fun testId_unscaled() = assertEquals(sut.expectedId, sut.quantity.id)
    { "${sut.quantity}.id unscaled" }

    private fun testId_scaled() = assertEquals(sut.expectedId, mega(sut.quantity).id)
    { "${sut.quantity}.id scaled" }

    private fun testUniqueId() {
        if (sut.isQuantityRepresentative) {
            assertFalse(collectedUnitIds.contains(sut.quantity.id))
            assertTrue(collectedUnitIds.size < allUnitIds.size)
            collectedUnitIds.add(sut.quantity.id)
        } else {
            assertTrue(allUnitIds.contains(sut.quantity.id))
            { "Quantity id of ${sut.quantity} not found in allUnitIds" }
        }
    }

    private data class TestedQuantity(
        val quantity: Quantity,
        val stringRep: String,
        val isQuantityRepresentative: Boolean,
        val expectedId: QuantityId) {
        constructor(quantity: Quantity, stringRep: String)
            : this(quantity, stringRep, isQuantityRepresentative = false, expectedId = quantity.id)

        val mustUseParens get() = when (stringRep.last()) {
            '4', '5', '6', '7', '8', '9', '²', '³' -> true
            else -> stringRep.contains('/')
        }

        fun quantityRepresentative()
            = TestedQuantity(
            quantity,
            stringRep,
            isQuantityRepresentative = true,
            expectedId = expectedId)

        infix fun withSameIdAs(other: Quantity)
            = TestedQuantity(
            quantity,
            stringRep,
            isQuantityRepresentative = isQuantityRepresentative,
            expectedId = other.id)
    }
    private var sut = TestedQuantity(one, "initial")
    private val testedQuantities = arrayOf(
        TestedQuantity(one, "1").quantityRepresentative(),
        TestedQuantity(radian, "rad") withSameIdAs one,
        TestedQuantity(steradian, "sr") withSameIdAs one,
        TestedQuantity(degree, "°") withSameIdAs one,
        TestedQuantity(bit, "bit").quantityRepresentative(),
        TestedQuantity(byte, "byte") withSameIdAs bit,
        TestedQuantity(mol, "mol").quantityRepresentative(),
        TestedQuantity(ampere, "A").quantityRepresentative(),
        TestedQuantity(meter, "m").quantityRepresentative(),
        TestedQuantity(inch, "\"") withSameIdAs meter,
        TestedQuantity(foot, "ft") withSameIdAs meter,
        TestedQuantity(yard, "yd") withSameIdAs meter,
        TestedQuantity(mile, "mile") withSameIdAs meter,
        TestedQuantity(nauticalMile, "NM") withSameIdAs meter,
        TestedQuantity(square(meter), "m²").quantityRepresentative(),
        TestedQuantity(are, "a") withSameIdAs square(meter),
        TestedQuantity(hectare, "ha") withSameIdAs square(meter),
        TestedQuantity(cubic(meter), "m³").quantityRepresentative(),
        TestedQuantity(liter, "l") withSameIdAs cubic(meter),
        TestedQuantity(meter / second, "m/s").quantityRepresentative(),
        TestedQuantity(knot, "knot") withSameIdAs meter / second,
        TestedQuantity(candela, "cd").quantityRepresentative(),
        TestedQuantity(gramm, "g").quantityRepresentative(),
        TestedQuantity(ton, "t") withSameIdAs gramm,
        TestedQuantity(pound, "lbs") withSameIdAs gramm,
        TestedQuantity(ounce, "oz") withSameIdAs gramm,
        TestedQuantity(poundAv, "lb. av.") withSameIdAs gramm,
        TestedQuantity(ounceAv, "oz. av.") withSameIdAs gramm,
        TestedQuantity(poundAp, "lb. ap.") withSameIdAs gramm,
        TestedQuantity(ounceAp, "oz. ap.") withSameIdAs gramm,
        TestedQuantity(kelvin, "K").quantityRepresentative(),
        TestedQuantity(second, "s").quantityRepresentative(),
        TestedQuantity(minute, "min") withSameIdAs second,
        TestedQuantity(hour, "h") withSameIdAs second,
        TestedQuantity(day, "d") withSameIdAs second,
        TestedQuantity(week, "wk") withSameIdAs second,
        TestedQuantity(year, "a") withSameIdAs second,
        TestedQuantity(newton, "N").quantityRepresentative() withSameIdAs kilo(gramm) * meter / square(second),
        TestedQuantity(poundForce, "lbf") withSameIdAs kilo(gramm) * meter / square(second),
        TestedQuantity(hertz, "Hz").quantityRepresentative() withSameIdAs one / second,
        TestedQuantity(pascal, "Pa").quantityRepresentative() withSameIdAs newton / square(meter),
        TestedQuantity(bar, "bar") withSameIdAs pascal,
        TestedQuantity(psi, "psi") withSameIdAs pascal,
        TestedQuantity(joule, "J").quantityRepresentative() withSameIdAs newton * meter,
        TestedQuantity(watt, "W").quantityRepresentative() withSameIdAs joule / second,
        TestedQuantity(coulomb, "C").quantityRepresentative() withSameIdAs ampere * second,
        TestedQuantity(volt, "V").quantityRepresentative() withSameIdAs watt / ampere,
        TestedQuantity(farad, "F").quantityRepresentative() withSameIdAs coulomb / volt,
        TestedQuantity(ohm, "Ω").quantityRepresentative() withSameIdAs volt / ampere,
        TestedQuantity(siemens, "S").quantityRepresentative() withSameIdAs ampere / volt,
        TestedQuantity(weber, "Wb").quantityRepresentative() withSameIdAs volt * second,
        TestedQuantity(tesla, "T").quantityRepresentative() withSameIdAs weber / square(meter),
        TestedQuantity(henry, "H").quantityRepresentative() withSameIdAs weber / ampere,
        TestedQuantity(lumen, "lm") withSameIdAs candela,
        TestedQuantity(lux, "lx").quantityRepresentative() withSameIdAs lumen / square(meter),
        TestedQuantity(becquerel, "Bq") withSameIdAs Reciproke(second),
        TestedQuantity(gray, "Gy").quantityRepresentative() withSameIdAs joule / kilo(gramm),
        TestedQuantity(sievert, "Sv") withSameIdAs joule / kilo(gramm),
        TestedQuantity(katal, "kat").quantityRepresentative() withSameIdAs mol / second)

    //region Private

    val collectedUnitIds = mutableSetOf<String>()
    val allUnitIds = testedQuantities.filter { it.isQuantityRepresentative }.map { it.quantity.id }.toHashSet()

    //endregion
}

