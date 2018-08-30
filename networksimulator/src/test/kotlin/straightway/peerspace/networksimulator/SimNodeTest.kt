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
package straightway.peerspace.networksimulator

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Key
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.KnownPeers
import straightway.peerspace.net.KnownPeersQuery
import straightway.peerspace.net.Peer
import straightway.peerspace.net.Request
import straightway.sim.net.Message
import straightway.sim.net.Node
import straightway.testing.bdd.Given
import straightway.testing.flow.References
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_
import straightway.units.byte
import straightway.units.get

class SimNodeTest : KoinLoggingDisabler() {

    private companion object {
        val id = Id("id")
    }

    private val test get() = Given {
        object {
            val existingInstances = mutableMapOf<Id, SimNode>()
            val peer = mock<Peer> { _ -> on { id }.thenReturn(id) }
            fun createSimNode() =
                    withContext {
                        bean { peer }
                        bean("simNodes") { existingInstances }
                    }.apply {
                        extraProperties["peerId"] = id.identifier
                    } make {
                        SimNode()
                    }
        }
    }

    @Test
    fun `construction adds to existing instances`() =
            test when_ {
                createSimNode()
            } then {
                expect(existingInstances.values has References(it.result))
            }

    @Test
    fun `construction adds to existing instances under id`() =
            test when_ {
                createSimNode()
            } then {
                expect(existingInstances[id] is_ Same as_ it.result)
            }

    @Test
    fun `notifyReceive forwards push to peer`() =
            testNotifyReceive(DataChunk(Key(Id("chunk")), byteArrayOf())) { pushDataChunk(it) }

    @Test
    fun `notifyReceive forwards query to peer`() =
            testNotifyReceive(DataQuery(Id("chunk"))) { queryData(it) }

    @Test
    fun `notifyReceive forwards known peers to peer`() =
            testNotifyReceive(KnownPeers(listOf(Id("knownPeer")))) { pushKnownPeers(it) }

    @Test
    fun `notifyReceive forwards known peers query to peer`() =
        testNotifyReceive(KnownPeersQuery()) { queryKnownPeers(it) }

    private fun <T : Transmittable> testNotifyReceive(
            received: T,
            checkedInvocation: Peer.(Request<T>) -> Unit
    ) {
        val senderId = Id("sender")
        test when_ {
            val sut = createSimNode()
            val sender = mock<Node> { on { id }.thenReturn(senderId) }
            sut.notifyReceive(sender, Message(received, 100[byte]))
        } then {
            val expectedRequest = Request.createDynamically(senderId, received)
            @Suppress("UNCHECKED_CAST")
            verify(peer).checkedInvocation(expectedRequest as Request<T>)
        }
    }
}