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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class NoneTest {
    @Test fun shortId_isEmpty() =
        Assertions.assertEquals("", None(meter).shortId)
    @Test fun scalue_alwaysUni() =
        Assertions.assertEquals(uni, None(kilo(meter)).scale)
    @Test fun hasExponent_0() =
        Assertions.assertEquals(0, None(meter).exponent)
    @Test fun toString_isEmpty() =
        Assertions.assertEquals("", None(meter).toString())

    @Test fun equals_true() =
        Assertions.assertTrue(None(meter).equals(None(meter)))
    @Test fun equals_null_false() =
        Assertions.assertFalse(None(meter).equals(null))
    @Test fun equals_otherType_false() =
        Assertions.assertFalse(None(meter).equals(None(second)))

    @Test fun times_Quantity() =
        Assertions.assertEquals(linear(meter), NoL * meter)
    @Test fun times_scaledQuantity() =
        Assertions.assertEquals(linear(kilo(meter)), NoL * (kilo(meter)))

    @Test fun div_Quantity() =
        Assertions.assertEquals(Reciproke(linear(meter)), NoL / meter)
    @Test fun div_scaledQuantity() =
        Assertions.assertEquals(Reciproke(linear(kilo(meter))), NoL / (kilo(meter)))
}