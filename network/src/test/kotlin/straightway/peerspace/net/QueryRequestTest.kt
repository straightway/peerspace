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
package straightway.peerspace.net

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.data.Identifyable
import straightway.peerspace.data.Key
import straightway.utils.deserializeTo
import straightway.utils.serializeToByteArray
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Null
import straightway.testing.flow.True
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.testing.testAutoGeneratedDataClassMethods

class QueryRequestTest {

    private companion object {
        val originatorId = Id("originatorId")
        val matchedId = Id("matchedId")
        val unmatchedId = Id("unmatchedId")
        val matchedIdentifyable = mock<Identifyable> {
            on { id }.thenReturn(matchedId)
        }
    }

    @Test
    fun testAutoGeneratedDataClassMethods() {
        val sut = QueryRequest(originatorId, matchedId, 1L..1L, epoch = 1)
        sut.testAutoGeneratedDataClassMethods()
    }

    @Test
    fun `construction with Id instance creates query with given originator id`() =
            Given { QueryRequest(originatorId, matchedId) } when_ {
                originatorId
            } then {
                expect(it.result is_ Equal to_ QueryRequestTest.originatorId)
            }

    @Test
    fun `construction with Id instance creates query for id`() =
            Given { QueryRequest(originatorId, matchedId) } when_ {
                id
            } then {
                expect(it.result is_ Equal to_ matchedId)
            }

    @Test
    fun `construction with Id instance creates query with zero timestamp range`() =
            Given { QueryRequest(originatorId, matchedId) } when_ {
                timestamps
            } then {
                expect(it.result is_ Equal to_ LongRange(0L, 0L))
            }

    @Test
    fun `construction with Identifyable creates query with given originator id`() =
            Given { QueryRequest(originatorId, matchedIdentifyable) } when_ {
                originatorId
            } then {
                expect(it.result is_ Equal to_ QueryRequestTest.originatorId)
            }

    @Test
    fun `construction with Identifyable creates query for id`() =
            Given { QueryRequest(originatorId, matchedIdentifyable) } when_ {
                id
            } then {
                expect(it.result is_ Equal to_ matchedId)
            }

    @Test
    fun `construction with Identifyable creates query with zero timestamp range`() =
            Given { QueryRequest(originatorId, matchedIdentifyable) } when_ {
                timestamps
            } then {
                expect(it.result is_ Equal to_ LongRange(0L, 0L))
            }

    @Test
    fun `construction with epoch and null timestamp range set null epoch`() =
            Given {
                QueryRequest(originatorId, matchedId, 0L..0L, 83)
            } when_ {
                epoch
            } then {
                expect(it.nullableResult is_ Null)
            }

    @Test
    fun `matches key with given id and zero timestamp`() =
            Given { QueryRequest(originatorId, matchedId, untimedData) } when_ {
                isMatching(Key(matchedId, 0))
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `does not match key with given id and non-zero timestamp`() =
            Given { QueryRequest(originatorId, matchedId, untimedData) } when_ {
                isMatching(Key(matchedId, 1))
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `does not match key with other id`() =
            Given { QueryRequest(originatorId, matchedId, untimedData) } when_ {
                isMatching(Key(unmatchedId, 0))
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `matches timestamp within range`() =
            Given { QueryRequest(originatorId, matchedId, 1L..2L) } when_ {
                isMatching(Key(matchedId, 1)) && isMatching(Key(matchedId, 2))
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `matches not timestamp outside range`() =
            Given { QueryRequest(originatorId, matchedId, 1L..2L) } when_ {
                isMatching(Key(matchedId, 0)) || isMatching(Key(matchedId, 3))
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `has serialVersionUID`() =
            expect(QueryRequest.serialVersionUID is_ Equal to_ 1L)

    @Test
    fun `is serializable`() =
            Given { QueryRequest(originatorId, matchedId, 1L..2L) } when_ {
                val serialized = serializeToByteArray()
                serialized.deserializeTo<QueryRequest>()
            } then {
                expect(it.result is_ Equal to_ this)
            }

    @Test
    fun `contains originator id`() =
            Given { QueryRequest(originatorId, matchedId, untimedData) } when_ {
                this.originatorId
            } then {
                expect(it.result is_ Equal to_ originatorId)
            }

    @Test
    fun `isUntimed if the timestamp range is 0L to 0L`() =
            Given { QueryRequest(originatorId, matchedId, untimedData) } when_ {
                isUntimed
            } then {
                expect(it.result is_ True)
            }

    @Test
    fun `isUntimed if the timestamp range is other than 0L to 0L`() =
            Given { QueryRequest(originatorId, matchedId, 1L..1L) } when_ {
                isUntimed
            } then {
                expect(it.result is_ False)
            }

    @Test
    fun `withEpoch returns queryRequest with given epoch set`() =
            Given { QueryRequest(originatorId, matchedId, 1L..1L) } when_ {
                withEpoch(1)
            } then {
                expect(it.result.epoch is_ Equal to_ 1)
            }

    @Test
    fun `withEpoch returns queryRequest with same originator`() =
            Given { QueryRequest(originatorId, matchedId, 1L..1L) } when_ {
                withEpoch(1)
            } then {
                expect(it.result.originatorId is_ Equal to_ originatorId)
            }

    @Test
    fun `withEpoch returns queryRequest with same matching chunk id`() =
            Given { QueryRequest(originatorId, matchedId, 1L..1L) } when_ {
                withEpoch(1)
            } then {
                expect(it.result.id is_ Equal to_ matchedId)
            }

    @Test
    fun `withEpoch returns queryRequest with same timestamps range`() =
            Given { QueryRequest(originatorId, matchedId, 1L..1L) } when_ {
                withEpoch(1)
            } then {
                expect(it.result.timestamps is_ Equal to_ 1L..1L)
            }

    @Test
    fun `withEpoch returns same queryRequest for untimed query requests`() =
            Given { QueryRequest(originatorId, matchedId) } when_ {
                withEpoch(1)
            } then {
                expect(it.result is_ Equal to_ QueryRequest(Companion.originatorId, matchedId))
            }

    @Test
    fun `toString for untimed query`() =
            expect(QueryRequest(Id("originator"), Id("chunk")).toString()
                           is_ Equal to_ "QueryRequest(chunk->originator)")

    @Test
    fun `toString for timed query`() =
            expect(QueryRequest(Id("originator"), Id("chunk"), 1L..2L).toString()
                           is_ Equal to_ "QueryRequest(chunk[1..2]->originator)")

    @Test
    fun `toString for timed query with epoch`() =
            expect(QueryRequest(Id("originator"), Id("chunk"), 1L..2L, 83).toString()
                           is_ Equal to_ "QueryRequest(chunk[1..2|83]->originator)")
}
