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
import org.junit.jupiter.api.Test
import straightway.expr.minus
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.koinutils.withContext
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QuerySource
import straightway.sim.net.TransmissionRequestHandler
import straightway.testing.bdd.Given
import straightway.testing.flow.Not
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.units.byte
import straightway.units.get

class SimNode_Factory_Test : KoinLoggingDisabler() {

    private companion object {
        val fromId = Id("fromPeer")
        val toId = Id("target")
        val notExistingId = Id("notExistingId")
    }

    private val test get() = Given {
        val nodes = mutableMapOf<Id, SimNode>()
        object {
            val pushTarget = mock<Peer>()
            val peers = mapOf(toId to pushTarget)
            val to = createSimNode(toId)
            val from = createSimNode(fromId)

            private fun createSimNode(id: Id): SimNode =
                    withContext {
                        bean { peers[it["id"]] as PushTarget }
                        bean { peers[it["id"]] as QuerySource }
                        bean("simNodes") { nodes }
                        bean { mock<TransmissionRequestHandler>() }
                        bean { chunkSizeGetter { 16[byte] } }
                    }.apply {
                        extraProperties["peerId"] = id.identifier
                    } make {
                        SimNode()
                    }
        }
    }

    @Test
    fun `createChannel returns new instance`() =
            test when_ { from.createChannel(toId) } then {
                expect(it.result.to is_ Same as_ to)
                expect(it.result.from is_ Same as_ from)
            }

    @Test
    fun `creating the same channel twice yields individual instances`() =
            test when_ { to.createChannel(fromId) } then {
                expect(it.result is_ Not - Same as_ to.createChannel(fromId))
            }

    @Test
    fun `getting a not existing channel throws an exception`() =
            test when_ { to.createChannel(notExistingId) } then {
                expect({ it.result } does Throw.exception)
            }
}