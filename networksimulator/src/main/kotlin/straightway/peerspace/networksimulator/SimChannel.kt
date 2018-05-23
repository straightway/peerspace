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

import straightway.peerspace.net.Channel
import straightway.peerspace.net.FinalTransmissionResult
import straightway.sim.net.Message
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.Node
import straightway.sim.net.Transmission
import straightway.units.AmountOfData
import straightway.units.UnitValue
import straightway.utils.Event
import java.io.Serializable

/**
 * A Channel implementation used for network simulation.
 */
class SimChannel(
        private val transmissionRequestHandler: TransmissionRequestHandler,
        private val chunkSizeGetter: (Serializable) -> UnitValue<Int, AmountOfData>,
        val from: Node,
        val to: Node
) : Channel {

    override val finished = Event<FinalTransmissionResult>()
    override fun transmit(data: Serializable) {
        transmissionRequestHandler.transmit(
                Transmission(from, to, Message(data, size = chunkSizeGetter(data))))
    }
}