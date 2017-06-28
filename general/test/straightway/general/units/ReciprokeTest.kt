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
import straightway.general.numbers.div

class ReciprokeTest {

    @Test fun id_containsNoScale() =
        assertEquals(Reciproke(square(meter)).id, Reciproke(square(kilo(meter))).id)

    @Test fun toString_unscaled() =
        assertEquals("1/km²", Reciproke(square(kilo(meter))).toString())

    @Test fun toString_scaled() =
        assertEquals("M(1/s)", mega(Reciproke(second)).toString())

    @Test fun toString_withSymbol() =
        assertEquals("Sym", Reciproke(second).withSymbol("Sym").toString())

    @Test fun toString_withSymbol_scaled() =
        assertEquals("kSym", kilo(Reciproke(second).withSymbol("Sym")).toString())

    @Test fun hasReciprokeScale() =
        assertEquals(milli, Reciproke(kilo(meter)).scale)

    @Test fun square_hasReciprokeScale() =
        assertEquals(micro, Reciproke(square(kilo(meter))).scale)

    @Test fun respectsCorrectedSiScale() =
        assertEquals(uni, Reciproke(kilo(gramm)).scale)

    @Test fun equals_true() =
        assertTrue(Reciproke(meter) == Reciproke(meter))

    @Suppress("SENSELESS_COMPARISON")
    @Test fun equals_null_false() =
        assertFalse(Reciproke(meter) == null)

    @Suppress("ReplaceCallWithComparison")
    @Test fun equals_differentTypes_false() =
        assertFalse(Reciproke(meter).equals("Hello"))

    @Test fun equals_differentBaseQuantity_false() =
        assertFalse(Reciproke(meter) == Reciproke(kelvin))

    @Test fun equals_sameBaseQuantity_differentScale_false() =
        assertFalse(Reciproke(meter) == Reciproke(kilo(meter)))

    @Test fun equals_sameBaseQuantity_differentExponent_false() =
        assertFalse(Reciproke(meter) == Reciproke(square(meter)))

    @Test fun equals_cancelledScale() =
        assertEquals(Reciproke(meter), kilo(Reciproke(kilo(meter))))

    @Test fun scale() =
        assertEquals(milli, Reciproke(kilo(meter)).scale)
    @Test fun scale_withCorrectedSiScale() =
        assertEquals(uni, Reciproke(kilo(gramm)).scale)

    @Test fun addScale() =
        assertEquals(milli, kilo(Reciproke(mega(meter))).scale)
    @Test fun rescale() =
        assertEquals(kilo, Reciproke(mega(meter)).withScale(kilo).scale)

    @Test fun withSymbol_scaled_setsBaseMagnitude() =
        assertEquals(milli.magnitude, Reciproke(kilo(meter)).withSymbol("Sym").baseMagnitude)

    @Test fun withSymbol_keepsShortId() =
        assertEquals((meter / second).id, (meter / second).withSymbol("X").id)

    @Test fun withSymbol_changesStringRepresentation() =
        assertEquals("X", (meter / second).withSymbol("X").toString())

    @Test fun scaled_withSymbol_changesStringRepresentation() =
        assertEquals("mX", milli((meter / second).withSymbol("X")).toString())

    @Test fun withSymbol_keepsScale() =
        assertEquals(deca, (kilo(meter) / hecto(second)).withSymbol("X").siScale)

    @Test fun withSymbol_respectsScaleCorrection() =
        assertEquals(uni, (kilo(gramm) / second).withSymbol("X").scale)

    @Test fun baseMagnitude() =
        assertEquals(1 / inch.baseMagnitude, Reciproke(inch).baseMagnitude)
}