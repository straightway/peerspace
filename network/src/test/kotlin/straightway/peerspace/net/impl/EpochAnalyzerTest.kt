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

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.TimeProvider
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class EpochAnalyzerTest : KoinLoggingDisabler() {

    private companion object {
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
                    on { now }.thenReturn(currentTime)
                }
                var sut = withContext {
                    bean { timeProvider }
                } make {
                    EpochAnalyzerImpl(epochs)
                }
            }
        }

    @Test
    fun `hashing for a single timestamp yields single epoch`() =
            test when_ {
                sut.getEpochs(83L..83L)
            } then {
                expect(it.result is_ Equal to_ listOf(5))
            }

    @Test
    fun `when two epochs overlap and the id is in the intersection, both are returned`() =
            test while_ {
                sut = withContext {
                    bean { timeProvider }
                } make {
                    EpochAnalyzerImpl(arrayOf(LongRange(1, 110), LongRange(90, 200)))
                }
            } when_ {
                val timestamp = currTimestamp - 100L
                sut.getEpochs(timestamp..timestamp)
            } then {
                expect(it.result is_ Equal to_ listOf(0, 1))
            }

    @Test
    fun `when the timestamp range overlaps two epochs, both are returned`() =
            test when_ {
                val epoch1 = getEpochBorders(1)
                val epoch2 = getEpochBorders(2)
                sut.getEpochs(epoch2.first..epoch1.endInclusive)
            } then {
                expect(it.result is_ Equal to_ listOf(1, 2))
            }

    @Test
    fun `when the timestamp range overlaps three epochs, all are returned`() =
            test when_ {
                val epoch1 = getEpochBorders(1)
                val epoch3 = getEpochBorders(3)
                sut.getEpochs(epoch3.first..epoch1.endInclusive)
            } then {
                expect(it.result is_ Equal to_ listOf(1, 2, 3))
            }

    @Test
    fun `timestamp before epoch 0 yields empty epoch list`() =
            test while_ {
                sut = withContext {
                    bean { timeProvider }
                } make {
                    EpochAnalyzerImpl(arrayOf(LongRange(50, 110), LongRange(90, 200)))
                }
            } when_ {
                sut.getEpochs(10L..20L)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `timestamp after last epoch yields empty epoch list`() =
            test while_ {
                sut = withContext {
                    bean { timeProvider }
                } make {
                    EpochAnalyzerImpl(arrayOf(LongRange(50, 110), LongRange(90, 200)))
                }
            } when_ {
                sut.getEpochs(310L..320L)
            } then {
                expect(it.result is_ Empty)
            }
}