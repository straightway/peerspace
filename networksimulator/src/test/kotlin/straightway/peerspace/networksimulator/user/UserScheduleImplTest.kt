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
package straightway.peerspace.networksimulator.user

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.hour
import straightway.utils.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class UserScheduleImplTest : KoinLoggingDisabler() {

    private companion object {
        val day = LocalDate.of(2022, 7, 23)
    }

    private val test get() =
        Given {
            object {
                var now = day.minusDays(3)
                val sut = withContext {
                    bean {
                        mock<TimeProvider> {
                            on { this.now }.thenAnswer { LocalDateTime.of(now, LocalTime.of(0, 0)) }
                        }
                    }
                } make {
                    UserScheduleImpl()
                }
            }
        }

    @Test
    fun `getBlockedTimes is initially empty`() =
        test when_ {
            sut.getBlockedTimes(day)
        } then {
            expect(it.result is_ Empty)
        }

    @Test
    fun `getBlockedTimes after single block call directly returns blocked range`() =
            test while_ {
                sut.block(day, 1[hour]..12[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(1[hour]..12[hour]))
            }

    @Test
    fun `blocking a range with negative start time starts the blocked range at 0h`() =
            test while_ {
                sut.block(day, -1[hour]..1[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(0[hour]..1[hour]))
            }

    @Test
    fun `blocking a range with negative end time is ignored`() =
            test while_ {
                sut.block(day, -2[hour]..-1[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `blocking a range with negative span is ignored`() =
            test while_ {
                sut.block(day, 2[hour]..1[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `second disjoint blocked time is added to existing ones`() =
            test while_ {
                sut.block(day, 1[hour]..2[hour])
                sut.block(day, 3[hour]..4[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_
                        Values(1[hour]..2[hour], 3[hour]..4[hour]))
            }

    @Test
    fun `second overlapping blocked time is merged with existing ones`() =
            test while_ {
                sut.block(day, 1[hour]..3[hour])
                sut.block(day, 2[hour]..4[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(1[hour]..4[hour]))
            }

    @Test
    fun `blocked times is empty if the only time range is for another day`() =
            test while_ {
                sut.block(day.plusDays(1), 1[hour]..2[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `blocked time for another day is ignored`() =
            test while_ {
                sut.block(day, 3[hour]..4[hour])
                sut.block(day.plusDays(1), 1[hour]..2[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(3[hour]..4[hour]))
            }

    @Test
    fun `a blocked range overlapping two days is fully visible in the first day`() =
            test while_ {
                sut.block(day, 23[hour]..25[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(23[hour]..25[hour]))
            }

    @Test
    fun `a blocked range overlapping two days is partly visible in the second day`() =
            test while_ {
                sut.block(day.minusDays(1), 23[hour]..25[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(0[hour]..1[hour]))
            }

    @Test
    fun `a blocked range within the next day is fully visible in the first day`() =
            test while_ {
                sut.block(day, 25[hour]..26[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(25[hour]..26[hour]))
            }

    @Test
    fun `a blocked range within the next day is fully visible in the next day`() =
            test while_ {
                sut.block(day.minusDays(1), 25[hour]..26[hour])
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Equal to_ Values(1[hour]..2[hour]))
            }

    @Test
    fun `blocked days from the past are cleared`() =
            test while_ {
                sut.block(day, 1[hour]..2[hour])
                now = day.plusDays(1)
            } when_ {
                sut.getBlockedTimes(day)
            } then {
                expect(it.result is_ Empty)
            }
}