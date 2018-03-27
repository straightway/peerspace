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

package straightway.peerspace.net

import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class PushRequestTest {

    private val test get() = Given {
        object {
            val chunk = Chunk(Key("4711"), "Hello")
            val sut = PushRequest(chunk)
        }
    }

    @Test
    fun `chunk is accessible`() =
            test when_ { sut } then { expect(it.result.chunk is_ Same as_ chunk) }

    @Test
    fun `has serialVersionUID`() =
        expect(PushRequest.serialVersionUID is_ Equal to_ 1L)

    @Test
    fun `is serializable`() =
            test when_
            {
                val serialized = sut.serializeToByteArray()
                serialized.deserializeTo<PushRequest>()
            } then {
                expect(it.result is_ Equal to_ sut)
            }
}