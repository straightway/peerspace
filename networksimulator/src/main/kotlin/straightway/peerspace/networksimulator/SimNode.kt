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

import straightway.error.Panic
import straightway.peerspace.data.Id
import straightway.peerspace.net.Channel
import straightway.peerspace.net.Factory
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PushRequest
import straightway.sim.net.Message
import straightway.sim.net.Node
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.TransmissionStream
import straightway.units.UnitValue
import straightway.units.AmountOfData
import java.io.Serializable

/**
 * Infrastructure for the simulation of a network peer.
 */
class SimNode(
        private val id: Id,
        private val peerInstances: Map<Id, Peer>,
        private val transmissionRequestHandler: TransmissionRequestHandler,
        private val chunkSizeGetter: (Serializable) -> UnitValue<Int, AmountOfData>,
        override val uploadStream: TransmissionStream,
        override val downloadStream: TransmissionStream,
        private val createdInstances: MutableMap<Id, SimNode>
) : Factory<Channel>, Node {

    override fun notifyReceive(sender: Node, message: Message) {
        message.content.let {
            when (it) {
                is PushRequest -> parent.push(it)
                else -> throw Panic("Invalid request: $it")
            }
        }
    }

    override fun create(id: Id) =
            SimChannel(
                    transmissionRequestHandler,
                    chunkSizeGetter,
                    from = this,
                    to = createdInstances[id]!!)

    private val parent get() = peerInstances[id]!!

    init {
        createdInstances[id] = this
    }
}