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

class ReciprokeTest {
    @Test fun Reciproke_hasNegatedExponent() =
        assertEquals(-2, Reciproke(square(meter)).exponent)
    @Test fun Reciproke_shortId() =
        assertEquals("1/m²", Reciproke(square(meter)).shortId)
    @Test fun Reciproke_shortId_containsNoScale() =
        assertEquals("1/m²", Reciproke(square(kilo(meter))).shortId)
    @Test fun Reciproke_toString() =
        assertEquals("1/(km)²", Reciproke(square(kilo(meter))).toString())
    @Test fun Reciproke_hasReciprokeScale() =
        assertEquals(milli, Reciproke(linear(kilo(meter))).scale)
    @Test fun Reciproke_square_hasReciprokeScale() =
        assertEquals(micro, Reciproke(square(kilo(meter))).scale)
    @Test fun Reciproke_respectsCorrectedSiScale() =
        assertEquals(uni, Reciproke(linear(kilo(gramm))).scale)

    @Test fun Reciproke_equals_true() =
        assertTrue(Reciproke(linear(meter)).equals(Reciproke(linear(meter))))
    @Test fun Reciproke_equals_null_false() =
        assertFalse(Reciproke(linear(meter)).equals(null))
    @Test fun Reciproke_equals_differentTypes_false() =
        assertFalse(Reciproke(linear(meter)).equals("Hello"))
    @Test fun Reciproke_equals_differentBaseQuantity_false() =
        assertFalse(Reciproke(linear(meter)).equals(Reciproke(linear(kelvin))))
    @Test fun Reciproke_equals_sameBaseQuantity_differentScale_false() =
        assertFalse(Reciproke(linear(meter)).equals(Reciproke(linear(kilo(meter)))))
    @Test fun Reciproke_equals_sameBaseQuantity_differentExponent_false() =
        assertFalse(Reciproke(linear(meter)).equals(Reciproke(square(meter))))

    @Test fun scale() =
        assertEquals(milli, reciproke(linear(kilo(meter))).scale)
    @Test fun scale_withCorrectedSiScale() =
        assertEquals(uni, reciproke(linear(kilo(gramm))).scale)
}