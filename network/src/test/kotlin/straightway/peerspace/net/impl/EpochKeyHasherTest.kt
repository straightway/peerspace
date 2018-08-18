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
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.untimedData
import straightway.peerspace.net.EpochAnalyzer
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class EpochKeyHasherTest : KoinLoggingDisabler() {

    private companion object {
        val id = Id("id")
    }

    private val test
        get() = Given {
            object {
                var hashCodes = byteArrayOf(0)
                val hasher = mock<Hasher> { _ ->
                    on { getHash(any()) }.thenAnswer { hashCodes }
                }
                var epochs = listOf(0)
                val epochAnalyzer = mock<EpochAnalyzer> { _ ->
                    on { getEpochs(any()) }.thenAnswer { epochs }
                }
                val hashable = DataQuery(id, 83L..83L)
                var sut = withContext {
                    bean { hasher }
                    bean { epochAnalyzer }
                } make {
                    EpochKeyHasher()
                }
            }
        }

    @Test
    fun `hashing for a single epoch yields a single hash code`() =
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
    fun `the hashcode of an id with zero timestamp calls hasher with DATA(id)`() =
            test when_ {
                sut.getHashes(DataQuery(id, untimedData))
            } then {
                verify(hasher).getHash("DATA($id)")
            }

    @Test
    fun `the hashcode with given epoch timestamp calls hasher with EPOCH?id)`() =
            listOf(0, 1, 2).forEach { testEpoch(it) }

    @Test
    fun `when the timestamp range overlaps multiple epochs, all of them are returned`() =
            test while_ {
                hashCodes = byteArrayOf(1)
                epochs = listOf(1, 2)
            } when_ {
                sut.getHashes(hashable)
            } then {
                expect(it.result is_ Equal to_ listOf(1L, 1L))
                inOrder(hasher) {
                    verify(hasher).getHash("EPOCH1($id)")
                    verify(hasher).getHash("EPOCH2($id)")
                }
            }

    @Test
    fun `when epoch is specified, use it and ignore time range`() =
            test when_ {
                sut.getHashes(
                        hashable.copy(
                            timestampsStart = Long.MIN_VALUE,
                            timestampsEndInclusive = Long.MAX_VALUE,
                            epoch = 12345))
            } then {
                verify(epochAnalyzer, never()).getEpochs(any())
                verify(hasher).getHash("EPOCH12345($id)")
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

    private fun testEpoch(epochIndex: Int) =
        test while_ {
            epochs = listOf(epochIndex)
        } when_ {
            sut.getHashes(hashable)
        } then {
            verify(hasher).getHash("EPOCH$epochIndex($id)")
        }

    private fun givenHashCodeFromBytes(vararg bytes: Byte) =
            test while_ {
                hashCodes = bytes
            } when_ {
                sut.getHashes(DataQuery(id, untimedData))
            }
}