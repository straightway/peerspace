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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test


class StandardQuantityTest {

    @Test fun defaultTests() {
        testedQuantities.forEach {
            sut = it
            testEquality()
            testComparison()
            testToString_scaled()
            testToString_unscaled()
        }
    }

    private fun testEquality() = assertEquals(1.0[sut.quantity], 100.0[centi(sut.quantity)])
    { "${sut.quantity::class}.equals" }

    private fun testComparison() = assertTrue(1.0[sut.quantity] > 10.0[centi(sut.quantity)])
    { "${sut.quantity::class}.compareTo" }

    private fun testToString_unscaled() = assertEquals("1 ${sut.shortId}".trimEnd(), 1[sut.quantity].toString())
    { "${sut.quantity::class}.toString" }

    private fun testToString_scaled() = assertEquals("1 m${sut.shortId}", 1[milli(sut.quantity)].toString())
    { "${sut.quantity::class}.toString" }

    private data class TestedQuantity(val quantity: RescalableQuantity, val shortId: String)
    private var sut = TestedQuantity(one, "initial")
    private val testedQuantities = arrayOf(
        TestedQuantity(one, ""),
        TestedQuantity(mol, "mol"),
        TestedQuantity(ampere, "A"),
        TestedQuantity(meter, "m"),
        TestedQuantity(candela, "cd"),
        TestedQuantity(kelvin, "K"))
}