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
import straightway.expr.minus
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataChunkQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.untimedData
import straightway.peerspace.data.chunkSize
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.dataChunkStore
import straightway.units.byte
import straightway.units.get
import straightway.units.times
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.Same
import straightway.testing.flow.Values
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.AmountOfData
import straightway.units.UnitValue
import straightway.units.mi

class TransientDataChunkStoreTest : KoinLoggingDisabler() {

    private companion object {
        val chunkId = Id("chunkId")
        val chunkData = "ChunkData".toByteArray()
        val otherChunkId = Id("otherId")
        const val chunkTimeStamp = 83L
    }

    private fun test(storageCapacity: UnitValue<AmountOfData> = 512[mi(byte)]) = Given {
        object {
            val environment = PeerTestEnvironment(
                    dataChunkStoreFactory = { TransientDataChunkStore() },
                    configurationFactory = { Configuration(storageCapacity = storageCapacity) }
            )
            val sut = environment.dataChunkStore as TransientDataChunkStore
            val untimedChunk = DataChunk(Key(chunkId), chunkData)
            val timedChunk = DataChunk(Key(chunkId, chunkTimeStamp), chunkData)
        }
    } while_ {
        sut.store(DataChunk(Key(otherChunkId), chunkData))
    }

    @Test
    fun `chunk can be stored`() =
            test() when_ { sut.store(untimedChunk) } then {
                expect(sut[untimedChunk.key] is_ Equal to_ untimedChunk)
            }

    @Test
    fun `query for not existing data is empty`() =
            test() when_ {
                sut.query(DataChunkQuery(chunkId, untimedData))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `query hit with untimed key`() =
            test() while_ {
                sut.store(untimedChunk)
            } when_ {
                sut.query(DataChunkQuery(chunkId, untimedData))
            } then {
                expect(it.result is_ Equal to_ Values(untimedChunk))
            }

    @Test
    fun `no query hit with wanted key but other timestamp`() =
            test() while_ {
                sut.store(timedChunk)
            } when_ {
                sut.query(DataChunkQuery(chunkId, 0L..(chunkTimeStamp - 1)))
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `query hit with wanted key and timestamp in range`() =
            test() while_ {
                sut.store(timedChunk)
            } when_ {
                sut.query(DataChunkQuery(chunkId, chunkTimeStamp..chunkTimeStamp))
            } then {
                expect(it.result is_ Equal to_ Values(timedChunk))
            }

    @Test
    fun `if capacity is exceeded, older chunk is removed an new chunk is stored`() =
            test(storageCapacity = chunkSize) when_ {
                sut.store(untimedChunk)
            } then {
                expect(sut[untimedChunk.key] is_ Same as_ untimedChunk)
                expect(sut[Key(otherChunkId)] is_ Null)
            }

    @Test
    fun `if capacity is not exceeded, new chunk is stored in addition`() =
            test(storageCapacity = 2 * chunkSize) when_ {
                sut.store(untimedChunk)
            } then {
                expect(sut[Key(otherChunkId)] is_ Not - Null)
                expect(sut[untimedChunk.key] is_ Same as_ untimedChunk)
            }

    @Test
    fun `if capacity is exceeded, only one chunk is removed`() =
            test(storageCapacity = 2 * chunkSize) while_ {
                sut.store(timedChunk)
            } when_ {
                sut.store(untimedChunk)
            } then {
                expect(sut[timedChunk.key] is_ Same as_ timedChunk)
                expect(sut[untimedChunk.key] is_ Same as_ untimedChunk)
                expect(sut[Key(otherChunkId)] is_ Null)
            }

    @Test
    fun `if capacity is exceeded, last queried chunks are considered new`() =
            test(storageCapacity = 2 * chunkSize) while_ {
                sut.store(timedChunk)
                sut.query(DataChunkQuery(otherChunkId))
            } when_ {
                sut.store(untimedChunk)
            } then {
                expect(sut[timedChunk.key] is_ Null)
                expect(sut[untimedChunk.key] is_ Same as_ untimedChunk)
                expect(sut[Key(otherChunkId)] is_ Not - Null)
            }

    @Test
    fun `adding the same chunk again changes removal order`() =
            test(storageCapacity = 2 * chunkSize) while_ {
                sut.store(timedChunk)
                sut.store(DataChunk(Key(otherChunkId), chunkData))
            } when_ {
                sut.store(untimedChunk)
            } then {
                expect(sut[timedChunk.key] is_ Null)
                expect(sut[untimedChunk.key] is_ Same as_ untimedChunk)
                expect(sut[Key(otherChunkId)] is_ Not - Null)
            }
}