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
package straightway.peerspace.net.impl

import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.PushRequest
import straightway.testing.bdd.Given

class DataPushForwarderImplTest : KoinLoggingDisabler() {

    private companion object {
        val pushRequest = PushRequest(Id("originatorId"), Chunk(Key(Id("chunkId")), byteArrayOf()))
    }

    private val test get() = Given {
        object {
            val environment =
                    PeerTestEnvironment(dataPushForwarderFactory = { DataPushForwarderImpl() })
            val sut =
                    environment.get<DataPushForwarder>()
            val dataQueryHandler =
                    environment.get<DataQueryHandler>()
            val pushForwardTracker =
                    environment.get<ForwardStateTracker<PushRequest, Key>>("pushForwardTracker")
        }
    }

    @Test
    fun `forwarding is handled by pushForwardTracker`() =
            test when_ {
                sut.forward(pushRequest)
            } then {
                verify(pushForwardTracker).forward(pushRequest)
            }

    @Test
    fun `forwarding notifies dataQueryHandler`() =
            test when_ {
                sut.forward(pushRequest)
            } then {
                verify(dataQueryHandler).notifyChunkForwarded(pushRequest.chunk.key)
            }
}