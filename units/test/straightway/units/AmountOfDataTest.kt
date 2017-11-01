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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AmountOfDataTest {
    @Test fun bit_toString() = Assertions.assertEquals("bit", bit.toString())
    @Test fun bit_scaling() = Assertions.assertEquals(1000[bit], 1[kilo(bit)])

    @Test fun byte_id() = Assertions.assertEquals(bit.id, byte.id)
    @Test fun byte_toString() = Assertions.assertEquals("byte", byte.toString())
    @Test fun byte_scaling() = Assertions.assertEquals(1000[byte], 1[kilo(byte)])
    @Test fun conversion_byte_to_bit() = Assertions.assertEquals(8, 1[byte][bit].value)
    @Test fun conversion_bit_to_byte() = Assertions.assertEquals(1, 8[bit][byte].value)
}