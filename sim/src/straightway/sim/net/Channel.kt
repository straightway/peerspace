/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.sim.net

import straightway.units.*

interface Channel {
    fun execute(request: TransmitRequest): UnitNumber<Time>
}

class TransmitRequest(val message: Message, val sender: Channel) {
    val receiver: Channel get() = _receiver!!
    infix fun to(receiver: Channel): TransmitRequest {
        this._receiver = receiver
        return this
    }

    val latency: UnitNumber<Time> get() = _latency
    infix fun withLatency(latency: UnitNumber<Time>): TransmitRequest {
        this._latency = latency
        return this
    }

    fun execute() = max(sender.execute(this), receiver.execute(this))

    private var _receiver: Channel? = null
    private var _latency: UnitNumber<Time> = 0[second]
}

infix fun Message.from(sender: Channel) = TransmitRequest(this, sender)
fun transmit(request: TransmitRequest) = request.execute()