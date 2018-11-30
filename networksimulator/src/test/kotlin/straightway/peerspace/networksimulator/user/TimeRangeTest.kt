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
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.hour

class TimeRangeTest {

    @Test
    fun `exclude from empty list remains empty`() =
            Given {
                listOf<TimeRange>()
            } when_ {
                exclude(1[hour]..2[hour])
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `exclude non-intersecting time range before from non-empty list returns receiver`() =
            Given {
                listOf<TimeRange>(4[hour]..5[hour])
            } when_ {
                exclude(1[hour]..2[hour])
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `exclude non-intersecting time range after from non-empty list returns receiver`() =
            Given {
                listOf<TimeRange>(4[hour]..5[hour])
            } when_ {
                exclude(7[hour]..8[hour])
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `exclude intersection at beginning of time range list`() =
            Given {
                listOf<TimeRange>(4[hour]..6[hour])
            } when_ {
                exclude(1[hour]..5[hour])
            } then {
                expect(it.result is_ Equal to_ listOf<TimeRange>(5[hour]..6[hour]))
            }

    @Test
    fun `exclude intersection at end of time range list`() =
            Given {
                listOf<TimeRange>(4[hour]..6[hour])
            } when_ {
                exclude(5[hour]..8[hour])
            } then {
                expect(it.result is_ Equal to_ listOf<TimeRange>(4[hour]..5[hour]))
            }

    @Test
    fun `negative range is ignored`() =
            Given {
                listOf<TimeRange>(7[hour]..4[hour])
            } when_ {
                exclude(5[hour]..6[hour])
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `exclude intersection in the middle of time range list`() =
            Given {
                listOf<TimeRange>(4[hour]..7[hour])
            } when_ {
                exclude(5[hour]..6[hour])
            } then {
                expect(it.result is_ Equal to_
                        listOf<TimeRange>(4[hour]..5[hour], 6[hour]..7[hour]))
            }

    @Test
    fun `exclude empty range from beginning is ignored`() =
            Given {
                listOf<TimeRange>(4[hour]..7[hour])
            } when_ {
                exclude(4[hour]..4[hour])
            } then {
                expect(it.result is_ Equal to_ listOf<TimeRange>(4[hour]..7[hour]))
            }

    @Test
    fun `exclusion in first time range keeps following ranges`() =
            Given {
                listOf<TimeRange>(4[hour]..7[hour], 9[hour]..14[hour])
            } when_ {
                exclude(5[hour]..6[hour])
            } then {
                expect(it.result is_ Equal to_
                        listOf<TimeRange>(4[hour]..5[hour], 6[hour]..7[hour], 9[hour]..14[hour]))
            }

    @Test
    fun `exclude from second range`() =
            Given {
                listOf<TimeRange>(4[hour]..7[hour], 9[hour]..14[hour])
            } when_ {
                exclude(8[hour]..12[hour])
            } then {
                expect(it.result is_ Equal to_
                        listOf<TimeRange>(4[hour]..7[hour], 12[hour]..14[hour]))
            }

    @Test
    fun `exclude beginning of second range`() =
            Given {
                listOf<TimeRange>(4[hour]..7[hour], 9[hour]..14[hour])
            } when_ {
                exclude(9[hour]..12[hour])
            } then {
                expect(it.result is_ Equal to_
                        listOf<TimeRange>(4[hour]..7[hour], 12[hour]..14[hour]))
            }

    @Test
    fun `exclude complete range with same end point`() =
            Given {
                listOf<TimeRange>(4[hour]..7[hour])
            } when_ {
                exclude(3[hour]..7[hour])
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `include in empty range list`() =
            Given {
                listOf<TimeRange>()
            } when_ {
                include(9[hour]..12[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..12[hour]))
            }

    @Test
    fun `include overlapping range before`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(7[hour]..10[hour])
            } then {
                expect(it.result is_ Equal to_ Values(7[hour]..12[hour]))
            }

    @Test
    fun `include overlapping range after`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(10[hour]..14[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..14[hour]))
            }

    @Test
    fun `include overlapping range on both edges`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(7[hour]..14[hour])
            } then {
                expect(it.result is_ Equal to_ Values(7[hour]..14[hour]))
            }

    @Test
    fun `include not overlapping range in the middle`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(10[hour]..11[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..12[hour]))
            }

    @Test
    fun `include adjacent range before`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(8[hour]..9[hour])
            } then {
                expect(it.result is_ Equal to_ Values(8[hour]..12[hour]))
            }

    @Test
    fun `include adjacent range after`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(12[hour]..14[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..14[hour]))
            }

    @Test
    fun `include disjoint range before`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(7[hour]..8[hour])
            } then {
                expect(it.result is_ Equal to_ Values(7[hour]..8[hour], 9[hour]..12[hour]))
            }

    @Test
    fun `include disjoint range after`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour])
            } when_ {
                include(14[hour]..18[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..12[hour], 14[hour]..18[hour]))
            }

    @Test
    fun `keep tail if adding to first range`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour], 14[hour]..18[hour])
            } when_ {
                include(10[hour]..11[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..12[hour], 14[hour]..18[hour]))
            }

    @Test
    fun `add to second range`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour], 14[hour]..18[hour])
            } when_ {
                include(15[hour]..19[hour])
            } then {
                expect(it.result is_ Equal to_ Values(9[hour]..12[hour], 14[hour]..19[hour]))
            }

    @Test
    fun `add before second range`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour], 15[hour]..18[hour])
            } when_ {
                include(13[hour]..14[hour])
            } then {
                expect(it.result is_ Equal to_
                        Values(9[hour]..12[hour], 13[hour]..14[hour], 15[hour]..18[hour]))
            }

    @Test
    fun `add after last range`() =
            Given {
                listOf<TimeRange>(9[hour]..12[hour], 13[hour]..14[hour])
            } when_ {
                include(15[hour]..18[hour])
            } then {
                expect(it.result is_ Equal to_
                        Values(9[hour]..12[hour], 13[hour]..14[hour], 15[hour]..18[hour]))
            }
}