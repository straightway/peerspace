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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class OneTest {

    @Test fun defaultScale_isUni() =
        assertEquals(uni, one.scale)
    @Test fun isScalable() =
        assertEquals(kilo, kilo(one).scale)
    @Test fun toString_unscaled() =
        assertEquals("1", one.toString())
    @Test fun toString_scaled() =
        assertEquals("k1", kilo(one).toString())
    @Test fun one_times_quantity_isQuantity() =
        assertEquals(mol, one * mol)
    @Test fun one_times_quantity_usesScaleOfQuantity() =
        assertEquals(kilo(mol), one * kilo(mol))
    @Test fun one_times_quantity_usesScaleOfReceiver() =
        assertEquals(kilo(mol), kilo(one) * mol)

    @Test fun quantity_times_one_isQuantity() =
        assertSame(mol, mol * one)

    @Test fun quantity_times_one_usesScaleOfQuantity() =
        assertEquals(kilo(mol), kilo(mol) * one)

    @Test fun quantity_times_one_usesScaleOfReceiver() =
        assertEquals(kilo(mol), mol * kilo(one))
    @Test fun one_div_quantity_isReciprokeQuantity() =
        assertEquals(Reciproke(mol), one / mol)
    @Test fun one_div_quantity_usesReciprokeScaleOfQuantity() =
        assertEquals(Reciproke(kilo(mol)), one / kilo(mol))
    @Test fun one_div_quantity_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / mol).scale)

    @Test fun one_div_reciproke_isQuantity() =
        assertEquals(mol, one / Reciproke(mol))

    @Test fun one_div_reciproke_usesReciprokeScaleOfQuantity() =
        assertEquals(kilo(mol), one / Reciproke(kilo(mol)))

    @Test fun one_div_div_reciproke_isIdentical() =
        assertEquals(Reciproke(kilo(mol)), one / (one / Reciproke(kilo(mol))))

    @Test fun one_div_div_reciproke_hasExpectedStringRepresentation() =
        assertEquals("1/kmol", (one / (one / Reciproke(kilo(mol)))).toString())

    @Test fun one_div_div_reciproke_withSymbol_hasExpectedStringRepresentation() =
        assertEquals("1/MHz", (one / mega(hertz)).toString())

    @Test fun one_div_div_reciproke_withSymbol_scaledReceiver() =
        assertEquals(Reciproke(kilo(hertz)), kilo(one) / mega(hertz))

    @Test fun one_div_div_reciproke_withSymbol_scaledReceiver_hasExpectedStringRepresentation() =
        assertEquals("1/kHz", (kilo(one) / mega(hertz)).toString())

    @Test fun one_div_reciproke_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / Reciproke(mol)).scale)

    @Test fun one_div_reciproke_withExplicitScale_usesScaleOfParameter() =
        assertEquals(milli(mol), one / Reciproke(mol).withScale(kilo))

    @Test fun one_div_product() =
        assertEquals(Reciproke(mol * second), one / (mol * second))

    @Test fun one_div_product_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / (mol * second)).scale)

    @Test fun one_div_product_usesScaleOfProduct() =
        assertEquals(Reciproke(kilo(mol) * second), one / (kilo(mol) * second))

    @Test fun one_div_product_toString_usesScaleOfProduct() =
        assertEquals("1/kmol*s", (one / (kilo(mol) * second)).toString())

    @Test fun reciproke_product_toString_usesScaleOfProduct() =
        assertEquals("1/kmol*s", Reciproke((kilo(mol) * second)).toString())

    @Test fun one_div_quotient() =
        assertEquals(mol / second, one / (second / mol))

    @Test fun one_div_quotient_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / (second / mol)).scale)

    @Test fun one_div_denominator_numerator() =
        assertEquals(second / meter, one / (Reciproke(second) * meter))

    @Test fun one_div_denominator_numerator_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / (Reciproke(second) * meter)).scale)

    @Test fun one_div_denominator_numeratorProduct() =
        assertEquals(mol * second / meter, one / (meter / (mol * second)))

    @Test fun one_div_left_nestedQuotient() =
        assertEquals(mol / (meter / second), one / ((meter / second) / mol))

    @Test fun one_div_plainProduct() =
        assertEquals(Reciproke(meter * second), one / (meter * second))

    @Test fun one_div_productOfReciprokes() =
        assertEquals(meter * second, one / (Reciproke(meter) * Reciproke(second)))

    @Test fun one_div_productOfReciprokes_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / (Reciproke(meter) * Reciproke(second))).scale)
}