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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.Peer
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class `PeerImpl general Test` : KoinLoggingDisabler() {

    private companion object {
        val id = Id("thePeerId")
        val dataQuery = QueryRequest(Id("queryingPeer"), Id("chunkId"))
        val knownPeersRequest = QueryRequest(Id("queryingPeerId"), Administrative.KnownPeers)
    }

    private val test get() = Given {
        PeerTestEnvironment(id, peerFactory = { PeerImpl() })
    }

    @Test
    fun `toString contains peer id`() =
            test when_ { get<Peer>().toString() } then {
                expect(it.result is_ Equal to_ "PeerImpl(${id.identifier})")
            }

    @Test
    fun `data queries are delegated to DataQueryHandler`() =
            test when_ { get<Peer>().query(dataQuery) } then {
                verify(get<DataQueryHandler>()).handle(dataQuery)
            }

    @Test
    fun `query notifies resultListener of success`() {
        val resultListener = mock<TransmissionResultListener>()
        test when_ { get<Peer>().query(dataQuery, resultListener) } then {
            verify(resultListener).notifySuccess()
            verify(resultListener, never()).notifyFailure()
        }
    }

    @Test
    fun `a query for known peers is delegated to the known peers provider`() =
            test when_ {
                get<Peer>().query(knownPeersRequest)
            } then {
                verify(get<KnownPeersProvider>()).pushKnownPeersTo(knownPeersRequest.originatorId)
            }
}