/*
 * Copyright 2016 github.com/straightway
 *
 *  Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package straightway.peerspace.data

import org.junit.jupiter.api.Test
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray

class IdTypeTest {

    private val test get() = Given {
        object {
            val sut = IdType.General
        }
    }

    @Test
    fun `is serializable`() =
            test when_ {
                sut.serializeToByteArray().deserializeTo<IdType>()
            } then {
                expect(it.result is_ Equal to_ sut)
            }

    @Test
    fun `has serialVersionUID`() =
            expect(IdType.serialVersionUID is_ Equal to_ 1L)
}