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
import straightway.peerspace.data.Id
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

class DataPushRequestTest {

    private fun test(timestamp: Long = 0L, epoch: Int? = null) = Given {
        object {
            val chunk = Chunk(Key(Id("4711"), timestamp, epoch), "Hello".toByteArray())
            val sut = DataPushRequest(Id("originatorId"), chunk)
        }
    }

    @Test
    fun `chunk is accessible`() =
            test() when_ { sut } then { expect(it.result.chunk is_ Same as_ chunk) }

    @Test
    fun `has serialVersionUID`() =
        expect(DataPushRequest.serialVersionUID is_ Equal to_ 1L)

    @Test
    fun `is serializable`() =
            test() when_
            {
                val serialized = sut.serializeToByteArray()
                serialized.deserializeTo<DataPushRequest>()
            } then {
                expect(it.result is_ Equal to_ sut)
            }

    @Test
    fun `withEpoch yields copy of push request with given epoch`() =
            test(timestamp = 83L) when_ {
                sut.withEpoch(2)
            } then {
                expect(it.result.chunk.key.epoch is_ Equal to_ 2)
            }

    @Test
    fun `withEpoch yields copy of push request with same chunk id`() =
            test(timestamp = 83L) when_ {
                sut.withEpoch(2)
            } then {
                expect(it.result.chunk.key.id is_ Equal to_ sut.chunk.key.id)
            }

    @Test
    fun `withEpoch yields copy of push request with same timestamp`() =
            test(timestamp = 83L) when_ {
                sut.withEpoch(2)
            } then {
                expect(it.result.chunk.key.timestamp is_ Equal to_ sut.chunk.key.timestamp)
            }

    @Test
    fun `withEpoch yields copy of push request with identical data`() =
            test(timestamp = 83L) when_ {
                sut.withEpoch(2)
            } then {
                expect(it.result.chunk.data is_ Same as_ sut.chunk.data)
            }

    @Test
    fun `withEpoch yields copy of push request with same originator id`() =
            test(timestamp = 83L) when_ {
                sut.withEpoch(2)
            } then {
                expect(it.result.originatorId is_ Equal to_ sut.originatorId)
            }

    @Test
    fun `identification is same as key`() =
            test() when_ {
                sut.identification
            } then {
                expect(it.result is_ Equal to_ sut.chunk.key)
            }
}