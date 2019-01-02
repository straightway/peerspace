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

class TimeRangesTest {

    @Test
    fun `exclude from empty list remains empty`() =
            Given {
                TimeRanges()
            } when_ {
                minusAssign(1.0[hour]..2.0[hour])
            } then {
                expect(this is_ Empty)
            }

    @Test
    fun `exclude non-intersecting time range before from non-empty list returns receiver`() =
            Given {
                TimeRanges()
            } when_ {
                minusAssign(1.0[hour]..2.0[hour])
            } then {
                expect(this is_ Equal to_ this)
            }

    @Test
    fun `exclude non-intersecting time range after from non-empty list returns receiver`() =
            Given {
                TimeRanges(4.0[hour]..5.0[hour])
            } when_ {
                minusAssign(7.0[hour]..8.0[hour])
            } then {
                expect(this is_ Equal to_ this)
            }

    @Test
    fun `exclude intersection at beginning of time range list`() =
            Given {
                TimeRanges(4.0[hour]..6.0[hour])
            } when_ {
                minusAssign(1.0[hour]..5.0[hour])
            } then {
                expect(this is_ Equal to_ Values(5.0[hour]..6.0[hour]))
            }

    @Test
    fun `exclude intersection at end of time range list`() =
            Given {
                TimeRanges(4.0[hour]..6.0[hour])
            } when_ {
                minusAssign(5.0[hour]..8.0[hour])
            } then {
                expect(this is_ Equal to_ Values(4.0[hour]..5.0[hour]))
            }

    @Test
    fun `negative range is ignored`() =
            Given {
                TimeRanges(7.0[hour]..4.0[hour])
            } when_ {
                minusAssign(5.0[hour]..6.0[hour])
            } then {
                expect(this is_ Empty)
            }

    @Test
    fun `exclude intersection in the middle of time range list`() =
            Given {
                TimeRanges(4.0[hour]..7.0[hour])
            } when_ {
                minusAssign(5.0[hour]..6.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(4.0[hour]..5.0[hour], 6.0[hour]..7.0[hour]))
            }

    @Test
    fun `exclude empty range from beginning is ignored`() =
            Given {
                TimeRanges(4.0[hour]..7.0[hour])
            } when_ {
                minusAssign(4.0[hour]..4.0[hour])
            } then {
                expect(this is_ Equal to_ Values(4.0[hour]..7.0[hour]))
            }

    @Test
    fun `exclusion in first time range keeps following ranges`() =
            Given {
                TimeRanges(4.0[hour]..7.0[hour], 9.0[hour]..14.0[hour])
            } when_ {
                minusAssign(5.0[hour]..6.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(4.0[hour]..5.0[hour], 6.0[hour]..7.0[hour], 9.0[hour]..14.0[hour]))
            }

    @Test
    fun `exclude from second range`() =
            Given {
                TimeRanges(4.0[hour]..7.0[hour], 9.0[hour]..14.0[hour])
            } when_ {
                minusAssign(8.0[hour]..12.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(4.0[hour]..7.0[hour], 12.0[hour]..14.0[hour]))
            }

    @Test
    fun `exclude beginning of second range`() =
            Given {
                TimeRanges(4.0[hour]..7.0[hour], 9.0[hour]..14.0[hour])
            } when_ {
                minusAssign(9.0[hour]..12.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(4.0[hour]..7.0[hour], 12.0[hour]..14.0[hour]))
            }

    @Test
    fun `exclude complete range with same end point`() =
            Given {
                TimeRanges(4.0[hour]..7.0[hour])
            } when_ {
                minusAssign(3.0[hour]..7.0[hour])
            } then {
                expect(this is_ Empty)
            }

    @Test
    fun `include in empty range list`() =
            Given {
                TimeRanges()
            } when_ {
                plusAssign(9.0[hour]..12.0[hour])
            } then {
                expect(this is_ Equal to_ Values(9.0[hour]..12.0[hour]))
            }

    @Test
    fun `include overlapping range before`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(7.0[hour]..10.0[hour])
            } then {
                expect(this is_ Equal to_ Values(7.0[hour]..12.0[hour]))
            }

    @Test
    fun `include overlapping range after`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(10.0[hour]..14.0[hour])
            } then {
                expect(this is_ Equal to_ Values(9.0[hour]..14.0[hour]))
            }

    @Test
    fun `include overlapping range on both edges`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(7.0[hour]..14.0[hour])
            } then {
                expect(this is_ Equal to_ Values(7.0[hour]..14.0[hour]))
            }

    @Test
    fun `include not overlapping range in the middle`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(10.0[hour]..11.0[hour])
            } then {
                expect(this is_ Equal to_ Values(9.0[hour]..12.0[hour]))
            }

    @Test
    fun `include adjacent range before`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(8.0[hour]..9.0[hour])
            } then {
                expect(this is_ Equal to_ Values(8.0[hour]..12.0[hour]))
            }

    @Test
    fun `include adjacent range after`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(12.0[hour]..14.0[hour])
            } then {
                expect(this is_ Equal to_ Values(9.0[hour]..14.0[hour]))
            }

    @Test
    fun `include disjoint range before`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(7.0[hour]..8.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(7.0[hour]..8.0[hour], 9.0[hour]..12.0[hour]))
            }

    @Test
    fun `include disjoint range after`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour])
            } when_ {
                plusAssign(14.0[hour]..18.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(9.0[hour]..12.0[hour], 14.0[hour]..18.0[hour]))
            }

    @Test
    fun `keep tail if adding to first range`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour], 14.0[hour]..18.0[hour])
            } when_ {
                plusAssign(10.0[hour]..11.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(9.0[hour]..12.0[hour], 14.0[hour]..18.0[hour]))
            }

    @Test
    fun `add to second range`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour], 14.0[hour]..18.0[hour])
            } when_ {
                plusAssign(15.0[hour]..19.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(9.0[hour]..12.0[hour], 14.0[hour]..19.0[hour]))
            }

    @Test
    fun `add before second range`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour], 15.0[hour]..18.0[hour])
            } when_ {
                plusAssign(13.0[hour]..14.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(9.0[hour]..12.0[hour], 13.0[hour]..14.0[hour],
                                15.0[hour]..18.0[hour]))
            }

    @Test
    fun `add after last range`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour], 13.0[hour]..14.0[hour])
            } when_ {
                plusAssign(15.0[hour]..18.0[hour])
            } then {
                expect(this is_ Equal to_
                        Values(9.0[hour]..12.0[hour], 13.0[hour]..14.0[hour],
                                15.0[hour]..18.0[hour]))
            }

    @Test
    fun `toString yields proper representation`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour], 13.0[hour]..14.0[hour])
            } when_ {
                toString()
            } then {
                expect(it.result is_ Equal to_ "TimeRanges[9.0 h..12.0 h, 13.0 h..14.0 h]")
            }

    @Test
    fun `size yields size`() =
            Given {
                TimeRanges(9.0[hour]..12.0[hour], 13.0[hour]..14.0[hour])
            } when_ {
                size
            } then {
                expect(it.result is_ Equal to_ 2)
            }
}