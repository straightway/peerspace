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
import org.junit.jupiter.api.Test

class AreaTest {
    @Test fun are_id() = assertEquals(square(meter).id, are.id)
    @Test fun are_toString() = assertEquals("a", are.toString())
    @Test fun are_scaling() = assertEquals(1000[are], 1[kilo(are)])
    @Test fun conversion_are_to_squareMeter() = assertEquals(100, 1[are][square(meter)].value)
    @Test fun conversion_squareMeter_to_are() = assertEquals(1, 100[square(meter)][are].value)

    @Test fun hectare_id() = assertEquals(square(meter).id, hectare.id)
    @Test fun hectare_toString() = assertEquals("ha", hectare.toString())
    @Test fun hectare_scaling() = assertEquals(1000[hectare], 1[kilo(hectare)])
    @Test fun conversion_hectare_to_squareMeter() = assertEquals(10000, 1[hectare][square(meter)].value)
    @Test fun conversion_squareMeter_to_hectare() = assertEquals(1, 10000[square(meter)][hectare].value)
}