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
import straightway.peerspace.net.PushRequest
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.QuerySource
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
        private val pushTargets: Map<Id, PushTarget>,
        private val querySources: Map<Id, QuerySource>,
        private val transmissionRequestHandler: TransmissionRequestHandler,
        private val chunkSizeGetter: (Serializable) -> UnitValue<Int, AmountOfData>,
        override val uploadStream: TransmissionStream,
        override val downloadStream: TransmissionStream,
        private val simNodes: MutableMap<Id, SimNode>
) : Factory<Channel>, Node {

    override var isOnline: Boolean
        get() = uploadStream.isOnline && downloadStream.isOnline
        set(new) {
            uploadStream.isOnline = new
            downloadStream.isOnline = new
        }

    override fun notifyReceive(sender: Node, message: Message) {
        message.content.let {
            when (it) {
                is PushRequest -> parentPushTarget.push(it)
                is QueryRequest -> parentQuerySource.query(it)
                else -> throw Panic("Invalid request: $it")
            }
        }
    }

    override fun create(id: Id) =
            SimChannel(
                    transmissionRequestHandler,
                    chunkSizeGetter,
                    from = this,
                    to = simNodes[id]!!)

    private val parentPushTarget get() = pushTargets[id]!!
    private val parentQuerySource get() = querySources[id]!!

    init {
        simNodes[id] = this
    }
}