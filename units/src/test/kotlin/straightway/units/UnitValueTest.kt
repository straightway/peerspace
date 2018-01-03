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
package straightway.units

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.testing.flow.*

class UnitValueTest {

    @Test fun construction_value() = assertEquals(2, UnitValue(2, testUnit).value)
    @Test fun construction_unit() = assertEquals(testUnit, UnitValue(2, testUnit).unit)
    @Test fun construction_withIndexer_value() = assertEquals(2, 2[testUnit].value)
    @Test fun construction_withIndexer_unit() = assertEquals(testUnit, 2[testUnit].unit)
    @Test fun construction_withScaledIndexer_value() = assertEquals(2, 2[kilo(testUnit)].value)
    @Test fun construction_withScaledIndexer_unit() = assertEquals(kilo(testUnit), 2[kilo(testUnit)].unit)

    @Test fun baseValue() = assertEquals(2000, 2 [kilo(testUnit)].baseValue)
    @Test fun baseValue_shifted() = assertEquals(3, 2[TestQuantity(uni, 1)].baseValue)
    @Test fun baseValue_scaled_shifted() = assertEquals(2001, 2[kilo(TestQuantity(uni, 1))].baseValue)

    @Test fun toString_unscaled() = assertEquals("2 TU", 2[testUnit].toString())
    @Test fun toString_scaled() = assertEquals("2 kTU", 2[kilo(testUnit)].toString())

    @Test fun equals_sameQuantity_sameScale() = assertTrue(2[testUnit] == 2[testUnit])
    @Test fun equals_sameQuantity_differentScale() = assertTrue(2000[testUnit] == 2[kilo(testUnit)])
    @Test fun equals_sameQuantity_differentUnit() = assertFalse(2[testUnit] == 2[otherTestUnit])
    @Test
    fun equals_sameQuantity_differentNumberTypes() = expect(11[testUnit] _is equal _to 1.1[deca(testUnit)])
    @Suppress("ReplaceCallWithComparison")
    @Test fun equals_differentTypes() = assertFalse(2000[testUnit].equals("Hallo"))
    @Test fun equals_differentProducts() = assertFalse(2000[meter * second] == 2000[mol * ampere])
    @Test fun equals_shiftedUnit() = assertTrue(0[TestQuantity(uni, 1)] == 1[TestQuantity(uni, 0)])

    @Test fun compare_sameScale_true() = assertTrue(1[testUnit] < 2[testUnit])
    @Test fun compare_sameScale_false() = assertFalse(2[testUnit] < 1[testUnit])
    @Test fun compare_differentScale() = assertTrue(2[testUnit] < 1[kilo(testUnit)])
    @Test
    fun compare_differentNumberTypes() = assertTrue(1.0[testUnit] < 2[testUnit])

    @Test fun siCorrectedUnit_value() = assertEquals(2, 2[siCorrectedTestUnit].value)
    @Test fun siCorrectedUnit_scaledValue() = assertEquals(2.0, 2.0[siCorrectedTestUnit].baseValue)
    @Test fun siCorrectedUnit_baseValue() = assertEquals(2e3, 2.0[siCorrectedTestUnit withScale uni].baseValue)

    @Test
    fun convert_sameUnit() = assertEquals(1, 1[testUnit][testUnit].value)

    @Test
    fun convert_targetScaled() = assertEquals(1_000, 1[testUnit][milli(testUnit)].value)

    @Test
    fun convert_sourceScaled() = assertEquals(1_000, 1[kilo(testUnit)][testUnit].value)

    @Test
    fun convert_bothScaled() = assertEquals(1_000_000, 1[kilo(testUnit)][milli(testUnit)].value)

    @Test
    fun convert_scaleCorrected() = assertEquals(1, 1[siCorrectedTestUnit][siCorrectedTestUnit].value)

    @Test
    fun convert_scaleCorrected_sourceScaled() = assertEquals(1_000, 1[kilo(siCorrectedTestUnit)][uni(siCorrectedTestUnit)].value)

    @Test
    fun convert_scaleCorrected_targetScaled() = assertEquals(1_000, 1[uni(siCorrectedTestUnit)][milli(siCorrectedTestUnit)].value)

    @Test
    fun convert_scaleCorrected_bothScaled() = assertEquals(1_000_000, 1[kilo(siCorrectedTestUnit)][milli(siCorrectedTestUnit)].value)

    @Test
    fun convert_sourceShifted() = assertEquals(2, 1[TestQuantity(uni, 1)][TestQuantity(uni, 0)].value)

    @Test
    fun convert_targetShifted() = assertEquals(1, 2[TestQuantity(uni, 0)][TestQuantity(uni, 1)].value)

    @Test
    fun convert_bothShifted() = assertEquals(1, 1[TestQuantity(uni, 1)][TestQuantity(uni, 1)].value)

    @Test
    fun convert_sourceShifted_sourceScaled() = assertEquals(1001, 1[kilo(TestQuantity(uni, 1))][TestQuantity(uni, 0)].value)

    @Test
    fun convert_sourceShifted_targetScaled() = assertEquals(0.002, 1[TestQuantity(uni, 1)][kilo(TestQuantity(uni, 0))].value)

    @Test
    fun convert_sourceShifted_bothScaled() = assertEquals(1.1, 1[deca(TestQuantity(uni, 1))][deca(TestQuantity(uni, 0))].value)

    @Test
    fun convert_targetShifted_sourceScaled() = assertEquals(999, 1[kilo(TestQuantity(uni, 0))][TestQuantity(uni, 1)].value)

    @Test
    fun convert_targetShifted_targetScaled() = assertEquals(0.001, 2[TestQuantity(uni, 0)][kilo(TestQuantity(uni, 1))].value)

    @Test
    fun convert_targetShifted_bothScaled() = assertEquals(0.999, 1[kilo(TestQuantity(uni, 0))][kilo(TestQuantity(uni, 1))].value)

    @Test
    fun convert_bothShifted_sourceScaled() = assertEquals(1000, 1[kilo(TestQuantity(uni, 1))][TestQuantity(uni, 1)].value)

    @Test
    fun convert_bothShifted_targetScaled() = assertEquals(0.002, 2[TestQuantity(uni, 1)][kilo(TestQuantity(uni, 1))].value)

    @Test
    fun convert_bothShifted_bothScaled() = assertEquals(1.0, 1[kilo(TestQuantity(uni, 1))][kilo(TestQuantity(uni, 1))].value)

    @Test
    fun convert_comatibleUnit_noScale_value() = expect(1[(meter * second) / second][meter].value _is equal _to 1.0)

    @Test
    fun convert_comatibleUnit_noScale_unit() = expect(1[(meter * second) / second][meter].unit _is equal _to meter)

    @Test
    fun convert_comatibleUnit_scaled_value() = expect(1[(meter * second) / second][milli(meter)].value _is equal _to 1000.0)

    @Test
    fun convert_comatibleUnit_scaled_unit() = expect(1[(meter * second) / second][milli(meter)].unit _is equal _to milli(meter))

    @Test
    fun convert_comatibleUnit_siCorrected_value() = expect(1[(kilo(gramm) * second) / second][gramm].value _is equal _to 1000.0)

    @Test
    fun convert_comatibleUnit_siCorrected_unit() = expect(1[(kilo(gramm) * second) / second][gramm].unit _is equal _to gramm)

    @Test
    fun convert_comatibleUnit_shifted_value() = expect(1[(kelvin * second) / second][kilo(celsius)].value _is equal _to -0.27215)

    @Test
    fun convert_comatibleUnit_shifted_unit() = expect(1[(kelvin * second) / second][celsius].unit _is equal _to celsius)

    @Test
    fun convert_comatibleUnit_withFactor_value() = expect(1[(pound * second) / second][kilo(gramm)].value _is equal _to 0.45359237)

    @Test
    fun convert_comatibleUnit_withFactor_unit() = expect(1[(pound * second) / second][kilo(gramm)].unit _is equal _to kilo(gramm))

    @Test
    fun convert_incomatibleUnit() = expect({ 1[(meter * second) / second][mol] } does _throw - Panic::class)

    @Test fun inFunctionParameter() {
        foo(2 [testUnit])
        foo(2.7 [mega(testUnit)])
        bar(7 [meter / second])
        bar(7 [kilo(meter) / hour])
        assertEquals(10.0[meter / second].baseValue, 36.0[kilo(meter) / hour].baseValue)
        assertTrue(10.0[meter / second] == 36.0[kilo(meter) / hour])
        assertTrue(36.0[meter / second] > 36.0[kilo(meter) / hour])
        assertTrue(36.0[kilo(meter) / hour] < 36.0[meter / second])
    }

    @Test
    fun add() {
        val a = 1[mol]
        val b = 1[deca(mol)]
        expect(a + b _is equal _to 11[mol])
    }

    @Test
    fun sub() {
        val a = 1[mol]
        val b = 1[deca(mol)]
        expect(a - b _is equal _to (-9)[mol])
    }

    @Test
    fun times_otherUnit() = expect(2[mol] * 3[second] _is equal _to 6[mol * second])

    @Test
    fun times_scalar_first() = expect(3 * (2[mol]) _is equal _to 6[mol])

    @Test
    fun times_scalar_second() = expect((2[fahrenheit] * 3) _is equal _to 6[fahrenheit])

    @Test
    fun div_otherUnit() = expect(6[mol] / 3[second] _is equal _to 2[mol / second])

    @Test
    fun div_scalar_first() = expect(1.0 / (2.0[mol]) _is equal _to 0.5[Reciproke(mol)])

    @Test
    fun div_scalar_second() = expect((2[fahrenheit] / 2) _is equal _to 1[fahrenheit])

    @Test
    fun unary_minus() = expect((-1)[mol] _is equal _to -1[mol])

    @Test
    fun unary_plus() = expect(1[mol] _is equal _to +1[mol])

    @Test
    fun min() = expect(min(2[mol], 1[mol]) _is equal _to 1[mol])

    @Test
    fun max() = expect(max(2[mol], 1[mol]) _is equal _to 2[mol])

    @Test
    fun abs_positive() = expect(abs(2[mol]) _is equal _to 2[mol])

    @Test
    fun abs_negative() = expect(abs(-2[mol]) _is equal _to 2[mol])

    //region Private

    @Suppress("UNUSED_PARAMETER")
    private fun foo(v: UnitValue<Number, TestQuantity>) {}

    @Suppress("UNUSED_PARAMETER")
    private fun bar(v: UnitValue<Number, Product<Length, Reciproke<Time>>>) {}

    private open class TestQuantity(scale: UnitScale, override val valueShift: Number)
        : QuantityBase("TU", scale, { TestQuantity(it, valueShift) })

    private val testUnit = TestQuantity(uni, 0)

    private open class SiCorrectedTestQuantity(scale: UnitScale, override val valueShift: Number)
        : QuantityBase("STU", scale, { SiCorrectedTestQuantity(it, valueShift) }) {
        override val siScaleCorrection = milli
    }

    private val siCorrectedTestUnit = SiCorrectedTestQuantity(milli, 0)

    private open class OtherTestQuantity(scale: UnitScale, override val valueShift: Number)
        : QuantityBase("OTU", scale, { OtherTestQuantity(it, valueShift) })

    private val otherTestUnit = OtherTestQuantity(uni, 0)

    //endregion
}