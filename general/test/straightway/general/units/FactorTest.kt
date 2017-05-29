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

class FactorTest {

    @Test fun wrapped() =
        assertEquals(meter, linear(meter).wrapped)
    @Test fun component1() =
        assertEquals(meter, linear(meter).component1())
    @Test fun component2() =
        assertEquals(NoL, linear(meter).component2())
    @Test fun rest() =
        assertEquals(NoL, linear(meter).rest)
    @Test fun hashCode_doesNotThrow() =
        linear(meter).hashCode()
    @Test fun copy() =
        assertEquals(linear(meter), linear(meter).copy())

    @Test fun equals_true() =
        assertTrue(linear(meter).equals(linear(meter)))
    @Test fun equals_null_false() =
        assertFalse(linear(meter).equals(null))
    @Test fun equals_differentTypes_false() =
        assertFalse(linear(meter).equals(linear(second)))
    @Test fun equals_differentBaseQuantity_false() =
        assertFalse(linear(meter).equals(linear(kelvin)))
    @Test fun equals_sameBaseQuantity_differentScale_false() =
        assertFalse(linear(meter).equals(linear(kilo(meter))))
    @Test fun equals_sameBaseQuantity_differentExponent_false() =
        assertFalse(linear(meter).equals(square(meter)))

    @Test fun linear_shortId_sameAsWrapped() =
        assertEquals("m", linear(meter).shortId)
    @Test fun linear_shortId_ofScaledUnit() =
        assertEquals("m", linear(kilo(meter)).shortId)
    @Test fun linear_hasExponent_1() =
        assertEquals(1, linear(meter).exponent)
    @Test fun linear_hasSameScaleAsWrapped() =
        assertEquals(kilo, linear(kilo(meter)).scale)
    @Test fun linear_respectsCorrectedSiScale() =
        assertEquals(uni, linear(kilo(gramm)).scale)
    @Test fun linear_toString_sameAsWrapped() =
        assertEquals("m", linear(meter).toString())
    @Test fun linear_toString_withScale() =
        assertEquals("km", linear(kilo(meter)).toString())

    @Test fun square_hasSquaredShortId() =
        assertEquals("m²", square(meter).shortId)
    @Test fun square_hasSquaredShortId_withScale() =
        assertEquals("m²", square(kilo(meter)).shortId)
    @Test fun Mass_square_hasSquaredShortId_withScale() =
        assertEquals("(kg)²", square(kilo(gramm)).shortId)
    @Test fun square_hasExponent_2() =
        assertEquals(2, square(meter).exponent)
    @Test fun square_hasSquaredScale() =
        assertEquals(mega, square(kilo(meter)).scale)
    @Test fun square_respectsCorrectedSiScale() =
        assertEquals(uni, square(kilo(gramm)).scale)
    @Test fun square_toString() =
        assertEquals("m²", square(meter).toString())
    @Test fun Mass_square_toString() =
        assertEquals("(kg)²", square(kilo(gramm)).toString())
    @Test fun square_toString_withScale() =
        assertEquals("(km)²", square(kilo(meter)).toString())

    @Test fun cubic_hasCubicShortId() =
        assertEquals("m³", cubic(meter).shortId)
    @Test fun cubic_hasExponent_3() =
        assertEquals(3, cubic(meter).exponent)
    @Test fun cubic_hasSquaredScale() =
        assertEquals(giga, cubic(kilo(meter)).scale)
    @Test fun cubic_respectsCorrectedSiScale() =
        assertEquals(uni, cubic(kilo(gramm)).scale)
    @Test fun cubic_toString() =
        assertEquals("m³", cubic(meter).toString())
    @Test fun cubic_toString_withScale() =
        assertEquals("(km)³", cubic(kilo(meter)).toString())

    @Test fun pow4_hasShortIdToThePowerOf4() =
        assertEquals("m^4", Factor(meter, cubic(meter)).shortId)
    @Test fun pow4_hasExponent_4() =
        assertEquals(4, Factor(meter, cubic(meter)).exponent)
    @Test fun pow4_hasSquaredScale() =
        assertEquals(tera, Factor(kilo(meter), cubic(kilo(meter))).scale)
    @Test fun pow4_respectsCorrectedSiScale() =
        assertEquals(uni, Factor(kilo(gramm), cubic(kilo(gramm))).scale)
    @Test fun pow4_toString() =
        assertEquals("m^4", Factor(meter, cubic(meter)).toString())
    @Test fun pow4_toString_withScale() =
        assertEquals("(km)^4", Factor(kilo(meter), cubic(kilo(meter))).toString())

    @Test fun scale_firstArgument() =
        assertEquals(kilo, Factor(kilo(meter), linear(meter)).scale)
    @Test fun scale_secondArgument() =
        assertEquals(kilo, Factor(meter, linear(kilo(meter))).scale)
    @Test fun scale_bothArguments() =
        assertEquals(mega, Factor(kilo(meter), linear(kilo(meter))).scale)

    @Test fun scale_correctedScale_firstArgument() =
        assertEquals(milli, Factor(kilo(gramm), linear(gramm)).scale)
    @Test fun scale_correctedScale_secondArgument() =
        assertEquals(milli, Factor(gramm, linear(kilo(gramm))).scale)
    @Test fun scale_correctedScale_bothArguments() =
        assertEquals(uni, Factor(kilo(gramm), linear(kilo(gramm))).scale)
}