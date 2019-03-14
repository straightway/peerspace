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

import straightway.peerspace.data.chunkSize
import straightway.peerspace.net.Channel
import straightway.peerspace.net.TransmissionResultListener
import straightway.sim.net.Message
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.Node
import straightway.sim.net.Transmission
import java.io.Serializable

/**
 * A Channel implementation used for network simulation.
 */
class SimChannel(
        private val transmissionRequestHandler: TransmissionRequestHandler,
        val from: Node,
        val to: Node
) : Channel {

    override fun transmit(data: Serializable, resultListener: TransmissionResultListener) {
        transmissionRequestHandler.transmit(Transmission(
                from.forwardNotificationsTo(resultListener),
                to,
                Message(data, size = chunkSize)))
    }

    private class NodeNotifier(
            private val wrapped: Node,
            private val resultListener: TransmissionResultListener
    ) : Node by wrapped {

        override fun notifySuccess(receiver: Node) {
            resultListener.notifySuccess()
            wrapped.notifySuccess(receiver)
        }

        override fun notifyFailure(receiver: Node) {
            resultListener.notifyFailure()
            wrapped.notifyFailure(receiver)
        }
    }

    private fun Node.forwardNotificationsTo(resultListener: TransmissionResultListener): Node =
            NodeNotifier(this, resultListener)
}