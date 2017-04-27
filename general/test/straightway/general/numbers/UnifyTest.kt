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
package straightway.general.numbers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigInteger

class UnifyTest {

    @Test fun numbersAreUnifiedToLargerType() {
        for ((i, a) in supportedTypes.withIndex())
            for ((j, b) in supportedTypes.withIndex()) {
                var unified = unify(a, b)
                var expectedType = if (i < j) b::class else a::class
                assertEquals(expectedType, unified.first::class) { "unifying ${a::class} and ${b::class}" }
                assertEquals(expectedType, unified.second::class) { "unifying ${a::class} and ${b::class}" }
                assertEquals(a.toDouble(), unified.first.toDouble())
                assertEquals(b.toDouble(), unified.second.toDouble())
            }
    }

    private companion object {
        val supportedTypes = arrayOf<Number>(
            83.toByte(),
            83.toShort(),
            83,
            83L,
            BigInteger("83"),
            83.0F,
            83.0
        )
    }
}