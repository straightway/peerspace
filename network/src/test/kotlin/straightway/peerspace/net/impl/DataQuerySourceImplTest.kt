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
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.Network
import straightway.peerspace.net.PeerDirectory
import straightway.testing.bdd.Given

class DataQuerySourceImplTest {

    private companion object {
        val id = Id("thePeerId")
        val dataQuery = DataQueryRequest(Id("queryingPeer"), DataQuery(Id("chunkId")))
    }

    private val test get() = Given {
        object {
            val environment = PeerTestEnvironment(
                id,
                dataQuerySourceFactory = { DataQuerySourceImpl() })
            val sut: DataQuerySource = environment.get()
        }
    }

    @Test
    fun `queries are delegated to DataQueryHandler`() =
            test when_ {
                sut.query(dataQuery)
            } then {
                verify(environment.get<DataQueryHandler>("dataQueryHandler")).handle(dataQuery)
            }

    @Test
    fun `query executes pending network requests`() =
            test when_ {
                sut.query(dataQuery)
            } then {
                verify(environment.get<Network>()).executePendingRequests()
            }

    @Test
    fun `originator of query request is added to known peers`() =
            test when_ {
                sut.query(dataQuery)
            } then {
                verify(environment.get<PeerDirectory>()).add(dataQuery.originatorId)
            }
}