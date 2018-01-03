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
package straightway.testing.flow

import org.junit.jupiter.api.Test
import straightway.dsl.minus
import straightway.testing.assertDoesNotThrow
import straightway.testing.assertFails
import java.time.Duration
import java.time.LocalDateTime

class RelationTest_isEqualTo {

    @Test
    fun passes() = assertDoesNotThrow { expect(1 _is equal _to 1) }

    @Test
    fun fails() = assertFails { expect(1 _is equal _to 2) }

    @Test
    fun negation_passes() = assertDoesNotThrow { expect(1 _is not - equal _to 2) }

    @Test
    fun number_range_smallerFirst_passes() = assertDoesNotThrow { expect(0.9 _is equalWithin(0.2) _to 1.0) }

    @Test
    fun number_range_biggerFirst_passes() = assertDoesNotThrow { expect(1.1 _is equalWithin(0.2) _to 1.0) }

    @Test
    fun number_range_fails() = assertFails { expect(1.3 _is equalWithin(0.2) _to 1.0) }

    @Test
    fun number_range_negation_passes() = assertDoesNotThrow { expect(1.3 _is not - equalWithin(0.2) _to 1.0) }

    @Test
    fun duration_range_smallerFirst_passes() = assertDoesNotThrow {
        expect(LocalDateTime.of(0, 1, 1, 0, 0, 0) _is equalWithin(Duration.ofDays(2)) _to LocalDateTime.of(0, 1, 2, 0, 0, 0))
    }

    @Test
    fun duration_range_biggerFirst_passes() = assertDoesNotThrow {
        expect(LocalDateTime.of(0, 1, 2, 0, 0, 0) _is equalWithin(Duration.ofDays(2)) _to LocalDateTime.of(0, 1, 1, 0, 0, 0))
    }

    @Test
    fun duration_range_fails() = assertFails {
        expect(LocalDateTime.of(0, 1, 1, 0, 0, 0) _is equalWithin(Duration.ofDays(2)) _to LocalDateTime.of(0, 1, 3, 0, 0, 0))
    }

    @Test
    fun duration_range_negation_passes() = assertDoesNotThrow {
        expect(LocalDateTime.of(0, 1, 1, 0, 0, 0) _is not - equalWithin(Duration.ofDays(2)) _to LocalDateTime.of(0, 1, 3, 0, 0, 0))
    }
}