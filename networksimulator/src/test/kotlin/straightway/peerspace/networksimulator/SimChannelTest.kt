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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argForWhich
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.net.TransmissionResultListener
import straightway.sim.net.Node
import straightway.sim.net.Transmission
import straightway.sim.net.TransmissionRequestHandler
import straightway.sim.net.TransmissionStream
import straightway.testing.bdd.Given
import straightway.units.byte
import straightway.units.get

class SimChannelTest {

    private val test get() = Given {
        object {
            var chunkSize = 16[byte]
            var transmitCallback: Transmission.() -> Unit = {}
            val net = mock<TransmissionRequestHandler> {
                on { transmit(any()) }.thenAnswer {
                    (it.arguments[0] as Transmission).transmitCallback()
                }
            }
            val fromUpload = mock<TransmissionStream>()
            val toDownload = mock<TransmissionStream>()
            val fromNode = mock<Node> { on { uploadStream }.thenReturn(fromUpload) }
            val toNode = mock<Node> { on { downloadStream }.thenReturn(toDownload) }
            val sut = SimChannel(net, { chunkSize }, fromNode, toNode)
        }
    }

    @Test
    fun `transmit sends data using the sender`() =
            test when_ { sut.transmit("Hello") } then {
                verify(net).transmit(any())
            }

    @Test
    fun `transmit sends from the origin node`() =
            test when_ { sut.transmit("Hello") } then {
                verify(net).transmit(argForWhich {
                    sender.uploadStream === fromNode.uploadStream &&
                    sender.downloadStream === fromNode.downloadStream
                })
            }

    @Test
    fun `transmit sends to the destination node`() =
            test when_ { sut.transmit("Hello") } then {
                verify(net).transmit(argForWhich { receiver === toNode })
            }

    @Test
    fun `transmit sends passed data`() =
            test when_ { sut.transmit("Hello") } then {
                verify(net).transmit(argForWhich { message.content == "Hello" })
            }

    @Test
    fun `transmit calls chunk size getter to get correct size`() =
            test when_ { sut.transmit("Hello") } then {
                verify(net).transmit(argForWhich { message.size == chunkSize })
            }

    @Test
    fun `transmit notifies resultListener of success`() {
        var resultListener = mock<TransmissionResultListener>()
        test while_ {
            transmitCallback = { sender.notifySuccess(receiver) }
        } when_ {
            sut.transmit("Hello", resultListener)
        } then {
            verify(resultListener).notifySuccess()
        }
    }

    @Test
    fun `transmit notifies sender node of success`() {
        test while_ {
            transmitCallback = { sender.notifySuccess(receiver) }
        } when_ {
            sut.transmit("Hello")
        } then {
            verify(fromNode).notifySuccess(toNode)
        }
    }

    @Test
    fun `transmit notifies resultListener of failure`() {
        var resultListener = mock<TransmissionResultListener>()
        test while_ {
            transmitCallback = { sender.notifyFailure(receiver) }
        } when_ {
            sut.transmit("Hello", resultListener)
        } then {
            verify(resultListener).notifyFailure()
        }
    }

    @Test
    fun `transmit notifies sender of failure`() {
        test while_ {
            transmitCallback = { sender.notifyFailure(receiver) }
        } when_ {
            sut.transmit("Hello")
        } then {
            verify(fromNode).notifyFailure(toNode)
        }
    }
}
