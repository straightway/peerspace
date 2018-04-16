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

import com.nhaarman.mockito_kotlin.mock
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.ForwardStrategy
import straightway.utils.TimeProvider
import java.time.LocalDateTime

/**
 * Implementation of the test environment for testing the PeerImpl class.
 */
data class PeerTestEnvironmentImpl(
        override val peerId: Id,
        override val knownPeersIds: List<Id> = listOf(),
        override val unknownPeerIds: List<Id> = listOf(),
        override var configuration: Configuration = Configuration(),
        override val localChunks: List<Chunk> = listOf(),
        val forwardStrategy: ForwardStrategy = mock(),
        override var timeProvider: TimeProvider = mock {
            on { currentTime }.thenReturn(LocalDateTime.of(2001, 1, 1, 14, 30))
        }
) : PeerTestEnvironment {
    override val knownPeers = knownPeersIds.map { createPeerMock(it) }
    override val unknownPeers = knownPeersIds.map { createPeerMock(it) }
    override var knownPeerQueryChooser = createChooser { knownPeersIds }
    override var knownPeerAnswerChooser = createChooser { knownPeersIds }
    override val infrastructure by lazy {
        createInfrastructure(
                peerDirectory = createPeerDirectory { knownPeers },
                network = createNetworkMock { knownPeers + unknownPeers },
                configuration = configuration,
                dataChunkStore = createChunkDataStore { localChunks },
                knownPeerQueryChooser = knownPeerQueryChooser,
                knownPeerAnswerChooser = knownPeerAnswerChooser,
                forwardStrategy = forwardStrategy,
                timeProvider = timeProvider)
    }
    override val sut by lazy { PeerImpl(peerId, infrastructure) }

    override fun getPeer(id: Id) =
            knownPeers.find { it.id == id } ?: unknownPeers.find { it.id == id }!!
}
