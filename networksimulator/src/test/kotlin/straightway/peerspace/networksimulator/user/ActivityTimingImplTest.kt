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

import org.junit.jupiter.api.Test
import straightway.error.Panic
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.random.toRandomStream
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Throw
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.UnitValue
import straightway.units.get
import straightway.units.hour
import straightway.units.minute

class ActivityTimingImplTest : KoinLoggingDisabler() {

    private fun <T : Number> List<ClosedRange<UnitValue<T, Time>>>.test(
            duration: UnitNumber<Time>,
            vararg randomSource: Double) =
        Given {
            object {
                val sut = withContext {
                    bean("randomSource") { randomSource.toList().toRandomStream().iterator() }
                } make {
                    ActivityTimingImpl(TimeRanges(map { it.asTimeRange() }), duration)
                }
            }
        }

    @Test
    fun `construction throws if no ranges are specified`() =
            expect({
                withContext {} make {
                    ActivityTimingImpl(TimeRanges(), 30[minute])
                }
            } does Throw.type<Panic>())

    @Test
    fun `single range, activity timed at start`() =
            listOf(1[hour]..2[hour]).test(30[minute], 0.0) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 1[hour]..1.5[hour])
            }

    @Test
    fun `single range, activity timed at end`() =
            listOf(1[hour]..2[hour]).test(30[minute], 1.0) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 1.5[hour]..2[hour])
            }

    @Test
    fun `two ranges, activity timed at start`() =
            listOf(1[hour]..2[hour], 4[hour]..5[hour]).test(30[minute], 0.0) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 1[hour]..1.5[hour])
            }

    @Test
    fun `two ranges, activity timed at end`() =
            listOf(1[hour]..2[hour], 4[hour]..5[hour]).test(30[minute], 1.0) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 4.5[hour]..5[hour])
            }

    @Test
    fun `two ranges, activity timed at end of first range`() =
            listOf(1[hour]..2[hour], 4[hour]..5[hour]).test(30[minute], 0.5) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 1.5[hour]..2[hour])
            }

    @Test
    fun `two adjacent ranges, activity timed at overlap`() =
            listOf(1[hour]..2[hour], 2[hour]..3[hour]).test(30[minute], 0.5) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 1.75[hour]..2.25[hour])
            }

    @Test
    fun `activity is too long`() =
            listOf(1[hour]..2[hour]).test(3[hour], 0.5) when_ {
                sut.timeRange
            } then {
                expect({ it.result } does Throw.type<DoesNotFitException>())
            }

    @Test
    fun `ranges not fitting with duration are ignored`() =
            listOf(1.0[hour]..1.1[hour], 4.0[hour]..5.0[hour]).test(30[minute], 0.0) when_ {
                sut.timeRange
            } then {
                expect(it.result[hour] is_ Equal to_ 4.0[hour]..4.5[hour])
            }

    private operator fun TimeRange.get(unit: Time) =
            start[unit]..endInclusive[unit]

    private fun <T : Number> ClosedRange<UnitValue<T, Time>>.asTimeRange() =
            (start as UnitNumber<Time>)..(endInclusive as UnitNumber<Time>)
}