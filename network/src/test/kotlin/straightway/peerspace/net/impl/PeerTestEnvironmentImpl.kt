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
import org.koin.Koin
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.IgnoreLogger
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.koinutils.get
import straightway.peerspace.koinutils.inject
import straightway.peerspace.koinutils.withContext
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.TransmissionResultListener
import straightway.utils.TimeProvider
import java.time.LocalDateTime

interface KoinProvider {
    val koin: KoinModuleComponent
}

/**
 * Implementation of the test environment for testing the PeerImpl class.
 */
data class PeerTestEnvironmentImpl(
        override val peerId: Id,
        override val knownPeersIds: List<Id> = listOf(),
        override val unknownPeerIds: List<Id> = listOf(),
        override var configuration: Configuration = Configuration(),
        override val localChunks: List<Chunk> = listOf(),
        override var forwardStrategy: ForwardStrategy = mock(),
        override var timeProvider: TimeProvider = mock {
            on { currentTime }.thenReturn(LocalDateTime.of(2001, 1, 1, 14, 30))
        },
        private var dataQueryHandlerFactory: () -> DataQueryHandler = { mock() },
        override var dataPushForwarder: DataPushForwarder = mock(),
        override var knownPeersProvider: KnownPeersProvider = mock()
) : PeerTestEnvironment, KoinProvider {

    override val koin = withContext {
        bean { dataQueryHandlerFactory() }
        bean { createChunkDataStore { localChunks } }
        bean {
            PeerImpl(createInfrastructure(
                peerDirectory = createPeerDirectory { knownPeers },
                network = createNetworkMock { knownPeers + unknownPeers },
                configuration = configuration,
                knownPeerQueryChooser = knownPeerQueryChooser,
                knownPeerAnswerChooser = knownPeerAnswerChooser,
                forwardStrategy = forwardStrategy,
                timeProvider = timeProvider,
                dataQueryHandler = get(),
                dataPushForwarder = dataPushForwarder,
                knownPeersProvider = knownPeersProvider))
        }
    }.apply {
        extraProperties["peerId"] = peerId.identifier
    } make { KoinModuleComponent() }

    override val dataQueryHandler: DataQueryHandler by koin.inject()
    override val knownPeers = knownPeersIds.map {
        createPeerMock(it) { pushRequest, resultForwarder ->
            pushTransmissionResultListeners[Pair(it, pushRequest.chunk.key)] = resultForwarder
        }
    }.toMutableList()
    override val unknownPeers = knownPeersIds.map { createPeerMock(it) }
    override var knownPeerQueryChooser = createChooser { knownPeersIds }
    override var knownPeerAnswerChooser = createChooser { knownPeersIds }
    override val infrastructure by lazy {
        createInfrastructure(
                peerDirectory = createPeerDirectory { knownPeers },
                network = createNetworkMock { knownPeers + unknownPeers },
                configuration = configuration,
                knownPeerQueryChooser = knownPeerQueryChooser,
                knownPeerAnswerChooser = knownPeerAnswerChooser,
                forwardStrategy = forwardStrategy,
                timeProvider = timeProvider,
                dataQueryHandler = dataQueryHandler,
                dataPushForwarder = dataPushForwarder,
                knownPeersProvider = knownPeersProvider)
    }

    override val peer by koin.inject<PeerImpl>()

    override fun setPeerPushSuccess(id: Id, success: Boolean) {
        peerPushSuccess[id] = success
    }
    override val pushTransmissionResultListeners =
            mutableMapOf<Pair<Id, Key>, TransmissionResultListener>()

    private val peerPushSuccess = mutableMapOf<Id, Boolean>()
}

fun <T : PeerTestEnvironment> T.fixed(): T {
    peer
    return this
}

val KoinProvider.dataChunkStore: DataChunkStore get() =
    koin.get()