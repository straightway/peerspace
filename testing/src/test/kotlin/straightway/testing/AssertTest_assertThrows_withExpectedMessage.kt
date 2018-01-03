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
package straightway.testing

import org.junit.jupiter.api.Test
import java.lang.NullPointerException
import java.security.InvalidKeyException

internal class AssertTest_assertThrows_withExpectedMessage {

    @Test
    fun passes_withExpectedExceptionTypeAndMessage() =
        assertDoesNotThrow {
            assertThrows<InvalidKeyException>("Expected") { throw InvalidKeyException("Expected") }
        }

    @Test
    fun fails_withUnexpectedExceptionMessage() =
        assertFails {
            assertThrows<InvalidKeyException>("Expected") { throw InvalidKeyException("Unexpected") }
        }

    @Test
    fun fails_withExceptionOfWrongType() =
        assertFails {
            assertThrows<NullPointerException>("Expected") { throw InvalidKeyException("Expected") }
        }

    @Test
    fun fails_withoutException() =
        assertFails { assertThrows<NullPointerException>("Expected") {} }
}
