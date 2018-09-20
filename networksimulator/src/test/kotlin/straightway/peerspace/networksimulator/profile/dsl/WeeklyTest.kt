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
package straightway.peerspace.networksimulator.profile.dsl

import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.True
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.get
import straightway.units.hour
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime

class WeeklyTest {

    @Test
    fun hours() =
            testProfile<Weekly> { Weekly(it) }
                    .testSingleValue<Weekly, ClosedRange<UnitNumber<Time>>>(1[hour]..3[hour]) {
                        hours
                    }

    @Test
    fun `update by invoke yields same instance`() =
            Given {
                Weekly {}
            } when_ {
                this { 1[hour]..2[hour] }
            } then {
                expect(it.result is_ Same as_ this)
            }

    @Test
    fun `update by invoke alters hours`() =
            Given {
                Weekly {}
            } when_ {
                this {
                    1[hour]..2[hour]
                }
            } then {
                expect(hours.value is_ Equal to_ 1[hour]..2[hour])
            }

    @Test
    fun `isApplicableTo yields true for matching LocalDateTime`() =
            Given {
                Weekly {
                    isApplicableTo { { _ -> true } }
                }
            } when_ {
                isApplicableTo(LocalDateTime.MAX)
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `isApplicableTo yields false for not matching LocalDateTime`() =
            Given {
                Weekly {
                    isApplicableTo { { _ -> false } }
                }
            } when_ {
                isApplicableTo(LocalDateTime.MAX)
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `isApplicableTo considers passed LocalDateTime`() {
        val testDateTime = LocalDateTime.of(2001, 1, 1, 17, 59)
        Given {
            Weekly {
                isApplicableTo { { dateTime -> expect(dateTime is_ Same as_ testDateTime); true } }
            }
        } when_ {
            isApplicableTo(testDateTime)
        } then {
            expect({ it.result } does Not - Throw.exception)
        }
    }

    @Test
    fun `mondays only applies to mondays`() =
            Weekly.mondays checkIsOnlyApplicableTo setOf(DayOfWeek.MONDAY)

    @Test
    fun `tuesdays only applies to tuesdays`() =
            Weekly.tuesdays checkIsOnlyApplicableTo setOf(DayOfWeek.TUESDAY)

    @Test
    fun `wednesdays only applies to wednesdays`() =
            Weekly.wednesdays checkIsOnlyApplicableTo setOf(DayOfWeek.WEDNESDAY)

    @Test
    fun `thursdays only applies to thursdays`() =
            Weekly.thursdays checkIsOnlyApplicableTo setOf(DayOfWeek.THURSDAY)

    @Test
    fun `fridays only applies to fridays`() =
            Weekly.fridays checkIsOnlyApplicableTo setOf(DayOfWeek.FRIDAY)

    @Test
    fun `saturdays only applies to saturdays`() =
            Weekly.saturdays checkIsOnlyApplicableTo setOf(DayOfWeek.SATURDAY)

    @Test
    fun `sundays only applies to sundays`() =
            Weekly.sundays checkIsOnlyApplicableTo setOf(DayOfWeek.SUNDAY)

    @Test
    fun `workdays only applies to mondays to fridays`() =
            Weekly.workdays checkIsOnlyApplicableTo setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY)

    @Test
    fun `weekends only applies to saturdays and sundays`() =
            Weekly.weekends checkIsOnlyApplicableTo setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    @Test
    fun `eachDay applies to all days`() =
            Weekly.eachDay checkIsOnlyApplicableTo setOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY)

    private infix fun Weekly.checkIsOnlyApplicableTo(daysToCheck: Set<DayOfWeek>) {
        val startTime = LocalDateTime.of(2018, 9, 16, 17, 59) // a sunday
        (1..7).map { DayOfWeek.of(it) }.forEach {
            val currTime = startTime + Duration.ofDays(it.value.toLong())
            expect(
                    isApplicableTo(currTime) is_ Equal to_
                            daysToCheck.contains(currTime.dayOfWeek))
        }
    }
}