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

class OneTest {

    @Test fun defaultScale_isUni() =
        assertEquals(uni, one.scale)
    @Test fun isScalable() =
        assertEquals(kilo, kilo(one).scale)
    @Test fun shortId_1() =
        assertEquals("1", one.shortId)
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
    @Test fun one_div_quantity_isReciprokeQuantity() =
        assertEquals(Reciproke(mol), one / mol)
    @Test fun one_div_quantity_usesReciprokeScaleOfQuantity() =
        assertEquals(milli, (one / kilo(mol)).scale)
    @Test fun one_div_quantity_usesScaleOfReceiver() =
        assertEquals(kilo, (kilo(one) / mol).scale)
}