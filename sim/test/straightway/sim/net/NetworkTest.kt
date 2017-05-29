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

import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.sim.core.Simulator
import straightway.testing.TestBase
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

class NetworkTest : TestBase<NetworkTest.Environment>() {

    class Environment {
        val simulator = Simulator()
        val network = Network(simulator)
    }

    @BeforeEach fun setup() {
        sut = Environment()
    }

    @Test fun send_schedulesEventForReceiver() {
        val sender = createSender(100)
        val receiver = createReceiver(100)
        val message = createMessage(100)
        sut.network.send(sender, receiver, message)
        sut.simulator.run()
        verify(receiver, times(1)).receive(sender, message)
    }

    @Test fun send_singleConnection_usesSenderUploadBandwidthIfLower() {
        val sender = createSender(200)
        val receiver = createReceiver(300)
        sut.network.send(sender, receiver, createMessage(100))
        verify(sut.simulator, times(1)).schedule(eq(Duration.ofMillis(500)), any())
    }

    @Test fun send_singleConnection_usesReceiverDownloadBandwidthIfLower() {
        val sender = createSender(300)
        val receiver = createReceiver(200)
        sut.network.send(sender, receiver, createMessage(100))
        verify(sut.simulator, times(1)).schedule(eq(Duration.ofMillis(500)), any())
    }

    @Test fun send_twoConnections_splitBandwithBetweenBoth() {
        val sender = createSender(300)
        val receiver = createReceiver(200)

        sut.network.send(sender, receiver, createMessage(100))
        sut.network.send(sender, receiver, createMessage(100))

        verify(sut.simulator, times(1)).schedule(eq(Duration.ofMillis(500)), any())
        verify(sut.simulator, times(2)).schedule(eq(Duration.ofMillis(1000)), any())
    }

    @Test fun send_twoConnections_receiveEventForFirstSchedulingIsCancelled() {
        val sender = createSender(300)
        val receiver = createReceiver(200)
        val messages = listOf(createMessage(100), createMessage(200))

        sut.network.send(sender, receiver, messages[0])
        sut.network.send(sender, receiver, messages[1])
        argumentCaptor<() -> Unit>().apply {
            verify(sut.simulator, times(3)).schedule(any(), capture())
            allValues.forEach { it() }
            verify(receiver, times(1)).receive(any(), eq(messages[0]))
            verify(receiver, times(1)).receive(any(), eq(messages[1]))
        }
    }

    /*@Test fun send_twoConnections_messageOfDifferentSizeArriveAtDifferentTimes() {
        val sender = createSender(300)
        val receiver = createReceiver(200)

        sut.network.send(sender, receiver, createMessage(100))
        sut.network.send(sender, receiver, createMessage(200))
        verify(sut.simulator, times(1)).schedule(eq(Duration.ofMillis(500)), any())
        verify(sut.simulator, times(1)).schedule(eq(Duration.ofMillis(1000)), any())
        verify(sut.simulator, times(1)).schedule(eq(Duration.ofMillis(1500)), any())
    }*/

    private data class ReceiveProtocolEntry(val time: LocalDateTime, val sender: Client, val message: Message)
    private inner class ClientMockWithReceiveProtocol(
        private val _uploadBytesPerSecond: Long?,
        private val _downloadBytesPerSecond: Long?) : Client {

        override val uploadBytesPerSecond get() = _uploadBytesPerSecond!!
        override val downloadBytesPerSecond get() = _downloadBytesPerSecond!!
        val receiveProtocol: List<ReceiveProtocolEntry> get() = _receiveProtocol

        override fun receive(sender: Client, message: Message) {
            _receiveProtocol += ReceiveProtocolEntry(sut.simulator.currentTime, sender, message)
        }

        val _receiveProtocol = mutableListOf<ReceiveProtocolEntry>()
    }

    private fun createSender(uploadBytesPerSecond: Long) =
        ClientMockWithReceiveProtocol(uploadBytesPerSecond, null)

    private fun createReceiver(downloadBytesPerSecond: Long) =
        ClientMockWithReceiveProtocol(null, downloadBytesPerSecond)

    private companion object {
        fun createMessage(sizeBytes: Long) =
            Message("Message(size=$sizeBytes Bytes, ID=${UUID.randomUUID()}", sizeBytes)
    }
}