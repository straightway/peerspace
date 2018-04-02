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
package straightway.peerspace.net.impl

import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.untimedData
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class TransientDataChunkStoreTest {

    private companion object {
        val peerId = Id("peerId")
        val receiverId = Id("receiverId")
        val chunkId = Id("chunkId")
        val chunkData = "ChunkData".toByteArray()
        const val chunkTimeStamp = 83L
    }

    private val test get() = Given {
        object {
            val sut = TransientDataChunkStore()
            val untimedChunk = Chunk(Key(chunkId), chunkData)
            val timedChunk = Chunk(Key(chunkId, chunkTimeStamp), chunkData)
        }
    } while_ {
        sut.store(Chunk(Key(Id("otherId")), chunkData))
    }

    @Test
    fun `chunk can be stored`() =
            test when_ { sut.store(untimedChunk) } then {
                expect(sut[untimedChunk.key] is_ Equal to_ untimedChunk)
            }

    @Test
    fun `query for not existing data is empty`() =
            test when_ {
                sut.query(QueryRequest(peerId, chunkId, untimedData))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `query hit with untimed key`() =
            test while_ {
                sut.store(untimedChunk)
            } when_ {
                sut.query(QueryRequest(receiverId, chunkId, untimedData))
            } then {
                expect(it.result is_ Equal to_ Values(untimedChunk))
            }

    @Test
    fun `no query hit with wanted key but other timestamp`() =
            test while_ {
                sut.store(timedChunk)
            } when_ {
                sut.query(QueryRequest(receiverId, chunkId, 0L..(chunkTimeStamp - 1)))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `query hit with wanted key and timestamp in range`() =
            test while_ {
                sut.store(timedChunk)
            } when_ {
                sut.query(QueryRequest(receiverId, chunkId, chunkTimeStamp..chunkTimeStamp))
            } then {
                expect(it.result is_ Equal to_ Values(timedChunk))
            }
}