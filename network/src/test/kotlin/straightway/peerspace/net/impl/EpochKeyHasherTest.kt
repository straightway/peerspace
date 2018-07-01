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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.koinutils.KoinLoggingDisabler
import straightway.peerspace.koinutils.withContext
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.untimedData
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.TimeProvider
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class EpochKeyHasherTest : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originatorId")
        val id = Id("id")
        val currentTime = LocalDateTime.of(2000, 1, 2, 3, 4, 5, 123456)!!
        val currTimestamp = ChronoUnit.MILLIS.between(LocalDateTime.of(0, 1, 1, 0, 0), currentTime)
        val epochs = arrayOf(
            LongRange(0L, 86400000L), // epoch 0: 1 day
            LongRange(86400001L, 604800000L), // epoch 1: 1 week
            LongRange(604800001L, 2419200000L), // epoch 2: 4 weeks
            LongRange(2419200001L, 54021600000L), // epoch 3: 1 year
            LongRange(54021600001L, 540216000000L), // epoch 4: 10 years
            LongRange(540216000001L, Long.MAX_VALUE) // epoch 5: more than 10 years
        )

        fun getEpochBorders(epochIndex: Int) = epochs[epochIndex].let {
            LongRange(currTimestamp - it.first, currTimestamp - it.endInclusive)
        }
    }

    private val test
        get() = Given {
            object {
                val timeProvider = mock<TimeProvider> {
                    on { currentTime }.thenReturn(currentTime)
                }
                var hashCodes = byteArrayOf(0)
                val hasher = mock<Hasher> {
                    on { getHash(any()) }.thenAnswer { hashCodes }
                }
                val hashable = QueryRequest(originatorId, id, 83L..83L)
                var sut = withContext {
                    bean { timeProvider }
                    bean { hasher }
                } make {
                    EpochKeyHasher(epochs)
                }
            }
        }

    @Test
    fun `hashing for a single timestamp yields a single hash code`() =
            test when_ {
                sut.getHashes(hashable)
            } then {
                expect(it.result.toList() is_ Equal to_ listOf(0L))
            }

    @Test
    fun `the hashcode is retrieved from the hasher`() =
            test while_ {
                hashCodes = byteArrayOf(1)
            } when_ {
                sut.getHashes(hashable)
            } then {
                verify(hasher).getHash(any())
                expect(it.result.toList() is_ Equal to_ listOf(1L))
            }

    @Test
    fun `the hashcode of an id with zero timestamp yields single hash code`() =
            test when_ {
                sut.getHashes(QueryRequest(originatorId, id, untimedData))
            } then {
                expect(it.result is_ Equal to_ listOf(0L))
            }

    @Test
    fun `the hashcode of an id with zero timestamp calls hasher with DATA(id)`() =
            test when_ {
                sut.getHashes(QueryRequest(originatorId, id, untimedData))
            } then {
                verify(hasher).getHash("DATA($id)")
            }

    @Test
    fun `the hash codes of a most recent query are identical to those of a timestamp query`() =
            test while_ {
                hashCodes = byteArrayOf(1)
            } when_ {
                sut.getHashes(QueryRequest.onlyMostRecent(originatorId, id))
            } then {
                expect(it.result is_ Equal to_ List(epochs.size) { 1L })
                inOrder(hasher) {
                    epochs.indices.forEach {
                        verify(hasher).getHash("EPOCH$it($id)")
                    }
                }
            }

    @Test
    fun `the hashcode with given epoch timestamp calls hasher with EPOCH?id)`() =
            epochs.indices.forEach { testEpoch(it) }

    @Test
    fun `when two epochs overlap and the id is in the intersection, both hashes are returned`() =
            test while_ {
                sut = withContext {
                    bean { timeProvider }
                    bean { hasher }
                } make {
                    EpochKeyHasher(arrayOf(LongRange(1, 110), LongRange(90, 200)))
                }
                hashCodes = byteArrayOf(1)
            } when_ {
                val timestamp = currTimestamp - 100L
                sut.getHashes(QueryRequest(originatorId, id, timestamp..timestamp))
            } then {
                expect(it.result is_ Equal to_ listOf(1L, 1L))
                inOrder(hasher) {
                    verify(hasher).getHash("EPOCH0($id)")
                    verify(hasher).getHash("EPOCH1($id)")
                }
            }

    @Test
    fun `when the timestamp range overlaps two epochs, both are returned`() =
            test while_ {
                hashCodes = byteArrayOf(1)
            } when_ {
                val epoch1 = getEpochBorders(1)
                val epoch2 = getEpochBorders(2)
                sut.getHashes(QueryRequest(originatorId, id, epoch2.first..epoch1.endInclusive))
            } then {
                expect(it.result is_ Equal to_ listOf(1L, 1L))
                inOrder(hasher) {
                    verify(hasher).getHash("EPOCH1($id)")
                    verify(hasher).getHash("EPOCH2($id)")
                }
            }

    @Test
    fun `when the timestamp range overlaps three epochs, all are returned`() =
            test while_ {
                hashCodes = byteArrayOf(1)
            } when_ {
                val epoch1 = getEpochBorders(1)
                val epoch3 = getEpochBorders(3)
                sut.getHashes(QueryRequest(originatorId, id, epoch3.first..epoch1.endInclusive))
            } then {
                expect(it.result is_ Equal to_ listOf(1L, 1L, 1L))
                inOrder(hasher) {
                    verify(hasher).getHash("EPOCH1($id)")
                    verify(hasher).getHash("EPOCH2($id)")
                    verify(hasher).getHash("EPOCH3($id)")
                }
            }

    @Test
    fun `foldToLong of empty bytes yields 0L`() =
            givenHashCodeFromBytes(0x0) then {
                expect(it.result is_ Equal to_ listOf(0L))
            }

    @Test
    fun `foldToLong of one byte bytes yields byte value`() =
            givenHashCodeFromBytes(83) then {
                expect(it.result is_ Equal to_ listOf(83L))
            }

    @Test
    fun `foldToLong of two byte bytes yields short value`() =
            givenHashCodeFromBytes(0x1, 0x2) then {
                expect(it.result is_ Equal to_ listOf(0x0201L))
            }

    @Test
    fun `foldToLong of three byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3) then {
                expect(it.result is_ Equal to_ listOf(0x030201L))
            }

    @Test
    fun `foldToLong of four byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3, 0x4) then {
                expect(it.result is_ Equal to_ listOf(0x04030201L))
            }

    @Test
    fun `foldToLong of five byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3, 0x4, 0x5) then {
                expect(it.result is_ Equal to_ listOf(0x0504030201L))
            }

    @Test
    fun `foldToLong of six byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3, 0x4, 0x5, 0x6) then {
                expect(it.result is_ Equal to_ listOf(0x060504030201L))
            }

    @Test
    fun `foldToLong of seven byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7) then {
                expect(it.result is_ Equal to_ listOf(0x07060504030201L))
            }

    @Test
    fun `foldToLong of eight byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8) then {
                expect(it.result is_ Equal to_ listOf(0x0807060504030201L))
            }

    @Test
    fun `foldToLong of nine byte bytes`() =
            givenHashCodeFromBytes(0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x10) then {
                expect(it.result is_ Equal to_ listOf(0x0807060504030211L))
            }

    private fun testEpoch(epochIndex: Int) {
        val epochBorders = epochs[epochIndex]
        val expectedHash = "EPOCH$epochIndex($id)"
        testEpochTimestamp(epochBorders.first, expectedHash)
        testEpochTimestamp(epochBorders.endInclusive, expectedHash)
    }

    private fun testEpochTimestamp(epochStart: Long, expectedHash: String) {
        test when_ {
            val timestamp = currTimestamp - epochStart
            sut.getHashes(QueryRequest(originatorId, id, timestamp..timestamp))
        } then {
            verify(hasher).getHash(expectedHash)
        }
    }

    private fun givenHashCodeFromBytes(vararg bytes: Byte) =
            test while_ {
                hashCodes = bytes
            } when_ {
                sut.getHashes(QueryRequest(originatorId, id, untimedData))
            }
}