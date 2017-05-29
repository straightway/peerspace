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

import straightway.sim.Scheduler
import java.lang.Math.min
import java.time.Duration

class Network(private val simScheduler: Scheduler) {

    fun send(sender: Client, receiver: Client, message: Message) {
        ++openConnections
        with (TransmissionInProgress(sender, receiver, message)) {
            for (n in 1..openConnections)
                scheduleDeliveryInSeconds(sendDurationSeconds)
        }
    }

    private val TransmissionInProgress.sendDurationSeconds: Double get() {
        val sendBytesPerSecond = min(receiver.downloadBytesPerSecond, sender.uploadBytesPerSecond)
        return (openConnections * message.bytes).toDouble() / sendBytesPerSecond.toDouble()
    }

    private fun TransmissionInProgress.scheduleDeliveryInSeconds(durationSeconds: Double) {
        simScheduler.schedule(Duration.ofNanos((NanosPerSecond * durationSeconds).toLong())) {
            deliverMessage()
        }
    }

    private fun TransmissionInProgress.deliverMessage() {
        if (0 < openConnections) {
            --openConnections
            receiver.receive(sender, message)
        }
    }

    private var openConnections = 0

    private companion object {
        const val NanosPerSecond = 1e9
    }

    private data class TransmissionInProgress(
        val sender: Client,
        val receiver: Client,
        val message: Message)
}