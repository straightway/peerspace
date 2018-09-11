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

import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.net.Request
import straightway.peerspace.net.dataQueryHandler
import straightway.peerspace.net.dataQuerySource
import straightway.peerspace.net.network
import straightway.peerspace.net.peerDirectory
import straightway.testing.bdd.Given

class DataQuerySourceImplTest {

    private companion object {
        val id = Id("thePeerId")
        val dataQuery = Request(Id("queryingPeer"), DataQuery(Id("chunkId")))
    }

    private val test get() = Given {
        object {
            val environment = PeerTestEnvironment(
                id,
                dataQuerySourceFactory = { DataQuerySourceImpl() })
            val sut = environment.dataQuerySource
        }
    }

    @Test
    fun `queries are delegated to DataQueryHandler`() =
            test when_ {
                sut.queryData(dataQuery)
            } then {
                verify(environment.dataQueryHandler).handle(dataQuery)
            }

    @Test
    fun `query executes pending network requests`() =
            test when_ {
                sut.queryData(dataQuery)
            } then {
                verify(environment.network).executePendingRequests()
            }

    @Test
    fun `originator of query request is added to known peers`() =
            test when_ {
                sut.queryData(dataQuery)
            } then {
                verify(environment.peerDirectory).add(dataQuery.remotePeerId)
            }
}