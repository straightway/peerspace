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
package straightway.peerspace.data

import org.junit.jupiter.api.Test
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.time.LocalDateTime

class TimeStampHelpersTest {

    @Test
    fun `timestamp of birth of christ is zero`() =
            Given {
                LocalDateTime.of(0, 1, 1, 0, 0, 0)
            } when_ {
                toTimestamp()
            } then {
                expect(it.result is_ Equal to_ 0L)
            }

    @Test
    fun `timestamp of 1 second after birth of christ is 1000`() =
            Given {
                LocalDateTime.of(0, 1, 1, 0, 0, 1)
            } when_ {
                toTimestamp()
            } then {
                expect(it.result is_ Equal to_ 1000L)
            }

    @Test
    fun `timestamp of 2023-02-03 11-12-13 AM`() =
            Given {
                LocalDateTime.of(2023, 2, 3, 11, 12, 13)
            } when_ {
                toTimestamp()
            } then {
                expect(it.result is_ Equal to_ 63842641933000L)
            }

    @Test
    fun `timestamp range`() =
            Given {
                LocalDateTime.of(0, 1, 1, 0, 0, 1)..LocalDateTime.of(0, 1, 1, 0, 0, 2)
            } when_ {
                toTimestamp()
            } then {
                expect(it.result is_ Equal to_ 1000L..2000L)
            }

    @Test
    fun `timestampRangeUntil timestamp range`() =
            Given {
                LocalDateTime.of(0, 1, 1, 0, 0, 1)
            } when_ {
                timestampRangeUntil(this)
            } then {
                expect(it.result is_ Equal to_ Long.MIN_VALUE..1000L)
            }

    @Test
    fun `timestampRangeFrom timestamp range`() =
            Given {
                LocalDateTime.of(0, 1, 1, 0, 0, 1)
            } when_ {
                timestampRangeFrom(this)
            } then {
                expect(it.result is_ Equal to_ 1000L..Long.MIN_VALUE)
            }
}