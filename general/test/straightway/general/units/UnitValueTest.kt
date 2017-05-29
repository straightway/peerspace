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

class UnitValueTest {

    @Test fun construction_value() = assertEquals(2, UnitValue(2, testUnit).value)
    @Test fun construction_unit() = assertEquals(testUnit, UnitValue(2, testUnit).unit)
    @Test fun construction_withIndexer_value() = assertEquals(2, 2[testUnit].value)
    @Test fun construction_withIndexer_unit() = assertEquals(testUnit, 2[testUnit].unit)
    @Test fun construction_withScaledIndexer_value() = assertEquals(2, 2[kilo(testUnit)].value)
    @Test fun construction_withScaledIndexer_unit() = assertEquals(kilo(testUnit), 2[kilo(testUnit)].unit)
    @Test fun scaledValue() = assertEquals(2000, 2 [kilo(testUnit)].scaledValue)
    @Test fun toString_unscaled() = assertEquals("2 TU", 2[testUnit].toString())
    @Test fun toString_scaled() = assertEquals("2 kTU", 2[kilo(testUnit)].toString())
    @Test fun equals_sameQuantity_sameScale() = assertTrue(2[testUnit].equals(2[testUnit]))
    @Test fun equals_sameQuantity_differentScale() = assertTrue(2000[testUnit].equals(2[kilo(testUnit)]))
    @Test fun equals_sameQuantity_differentUnit() = assertFalse(2[testUnit].equals(2[otherTestUnit]))
    @Test fun equals_differentTypes() = assertFalse(2000[testUnit].equals("Hallo"))
    @Test fun compare_sameScale_true() = assertTrue(1[testUnit] < 2[testUnit])
    @Test fun compare_sameScale_false() = assertFalse(2[testUnit] < 1[testUnit])
    @Test fun compare_differentScale() = assertTrue(2[testUnit] < 1[kilo(testUnit)])
    @Test fun siCorrectedUnit_value() = assertEquals(2, 2[siCorrectedTestUnit].value)
    @Test fun siCorrectedUnit_scaledValue() = assertEquals(2.0, 2.0[siCorrectedTestUnit].scaledValue)
    @Test fun siCorrectedUnit_baseValue() = assertEquals(2e3, 2.0[uni(siCorrectedTestUnit)].scaledValue)

    @Test fun inFunctionParameter() {
        foo(2 [testUnit])
        foo(2.7 [mega(testUnit)])
        //bar(7 [meter / second])
        //bar(7 [kilo(meter) / hour])
        //assertEquals(36[meter/second].scaledValue, 10[kilo(meter)/hour].scaledValue)
    }

    @Suppress("UNUSED")
    private fun foo(v: UnitValue<Number, TestQuantity>) {}
    //private fun bar(v: UnitValue<Number, Quotient<Length, Time>>) {}

    private open class TestQuantity(scale: UnitScale) : QuantityBase("TU", scale, { TestQuantity(it) })
    private val testUnit = TestQuantity(uni)

    private open class SiCorrectedTestQuantity(scale: UnitScale) : QuantityBase("STU", scale, { SiCorrectedTestQuantity(it) }) {
        override val siScaleCorrection: UnitScale get() = milli
    }
    private val siCorrectedTestUnit = SiCorrectedTestQuantity(milli)

    private open class OtherTestQuantity(scale: UnitScale) : QuantityBase("OTU", scale, { OtherTestQuantity(it) })
    private val otherTestUnit = OtherTestQuantity(uni)
}