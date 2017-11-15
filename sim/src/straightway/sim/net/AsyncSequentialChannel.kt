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

import straightway.sim.core.*
import straightway.units.*
import java.time.LocalDateTime

class AsyncSequentialChannel(private val bandwidth: UnitValue<Int, Bandwidth>) : Channel {

    override fun requestTransmission(request: TransmitRequest) = request.createOffer()
    override fun accept(offer: TransmitOffer) = if (offer.isMyOwn) acceptOwn(offer) else acceptForeign(offer)

    private data class TransmissionRecord(val startTime: LocalDateTime, val duration: UnitNumber<Time>) {
        val endTime by lazy { startTime + duration }
    }

    private fun acceptOwn(offer: TransmitOffer) {
        scheduledTransmissions = offer.transmissions
    }

    private fun acceptForeign(offer: TransmitOffer) {
        scheduledTransmissions += TransmissionRecord(offer.startTime, offer.request.duration)
    }

    private fun TransmitRequest.createRecord() = TransmissionRecord(startTime, duration)

    private fun TransmitRequest.createOffer() = TransmitOffer(
        issuer = this@AsyncSequentialChannel,
        finishTime = endTime,
        request = this,
        memento = scheduledTransmissions + createRecord())

    private val TransmitRequest.duration get() = (message.size / bandwidth)[second]
    private val TransmitRequest.receiverLatency get() = if (isReceiver) latency else 0[second]
    private val TransmitRequest.isReceiver get() = receiver === this@AsyncSequentialChannel
    private val TransmitRequest.startTime get() = startTimeOfFirstTransmissionBlock + receiverLatency
    private val TransmitRequest.endTime get() = startTime + duration

    private val TransmitOffer.startTime get() = finishTime - request.duration
    @Suppress("UNCHECKED_CAST")
    private val TransmitOffer.transmissions
        get() = memento as List<TransmissionRecord>
    private val TransmitOffer.isMyOwn get() = issuer === this@AsyncSequentialChannel

    private val firstTransmissionStartTime get() = scheduledTransmissions.firstOrNull()?.startTime ?: currTime
    private val allTransmissionsFinishedTime get() = scheduledTransmissions.lastOrNull()?.endTime ?: currTime
    private val startTimeOfFirstTransmissionBlock
        get() =
            if (currTime.isBefore(firstTransmissionStartTime)) currTime else allTransmissionsFinishedTime
    private val timeProvider = Simulator()
    private val currTime get() = timeProvider.currentTime

    private var scheduledTransmissions = listOf<TransmissionRecord>()
}