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
package straightway.test.flow

import org.junit.jupiter.api.Test
import straightway.general.dsl.minus
import straightway.test.assertDoesNotThrow
import straightway.test.assertFails

class RelationTest_empty {

    @Test fun succeeds_onEmptyCollection() =
            assertDoesNotThrow { expect(listOf<Int>() _is empty) }

    @Test fun fails_onNonEmptyCollection() =
            assertFails { expect(listOf(1) _is empty) }

    @Test fun negationSucceeds_onNonEmptyCollection() =
            assertDoesNotThrow { expect(listOf(1) _is not-empty) }

    @Test fun negationFails_onEmptyCollection() =
        assertFails { expect(listOf<Int>() _is not-empty) }

    @Test fun succeeds_onEmptyArray() =
        assertDoesNotThrow { expect(arrayOf<Int>() _is empty) }

    @Test fun fails_onNonEmptyArray() =
        assertFails { expect(arrayOf(1) _is empty) }

    @Test fun negationSucceeds_onNonEmptyArray() =
        assertDoesNotThrow { expect(arrayOf(1) _is not-empty) }

    @Test fun negationFails_onEmptyArray() =
        assertFails { expect(arrayOf<Int>() _is not-empty) }

    @Test fun succeeds_onEmptyString() =
        assertDoesNotThrow { expect("" _is empty) }

    @Test fun fails_onNonEmptyString() =
        assertFails { expect("Hello" _is empty) }

    @Test fun negationSucceeds_onNonEmptyString() =
        assertDoesNotThrow { expect("Hello" _is not-empty) }

    @Test fun negationFails_onEmptyString() =
        assertFails { expect("" _is not-empty) }
}