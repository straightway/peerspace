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

import org.junit.jupiter.api.Test
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.testing.testAutoGeneratedDataClassMethods
import java.time.LocalDateTime

class PendingDataQueryTest {

    @Test
    fun testAutoGeneratedDataClassMethods() =
            sut.testAutoGeneratedDataClassMethods()

    @Test
    fun getforwardedChunkKeys() =
            expect(sut.forwardedChunkKeys is_ Equal to_ setOf<Key>())

    @Test
    fun getReceiveTime() =
            expect(sut.receiveTime is_ Equal to_ LocalDateTime.MIN)

    @Test
    fun getQuery() =
            expect(sut.query is_ Equal to_ query)

    @Test
    fun `toString without forwarded chunk keys`() =
            expect(sut.toString() is_ Equal to_ "Pending(${sut.query}@${sut.receiveTime})")

    @Test
    fun `toString with forwarded chunk keys`() {
        val key1 = Key(Id("forwardedId1"))
        val key2 = Key(Id("forwardedId2"))
        expect(sut.copy(forwardedChunkKeys = setOf(key2, key1)).toString()
                       is_ Equal to_ "Pending(${sut.query}@${sut.receiveTime}:" +
                       "$key1,$key2)")
    }

    @Test
    fun `toString with forwarded chunk keys in different orders`() {
        val key1 = Key(Id("forwardedId1"))
        val key2 = Key(Id("forwardedId2"))
        expect(sut.copy(forwardedChunkKeys = setOf(key1, key2)).toString() is_ Equal to_
                       sut.copy(forwardedChunkKeys = setOf(key2, key1)).toString())
    }

    private val query = Request(Id("originatorId"), DataQuery(Id("chunkId")))
    private val sut = PendingDataQuery(query, LocalDateTime.MIN)
}