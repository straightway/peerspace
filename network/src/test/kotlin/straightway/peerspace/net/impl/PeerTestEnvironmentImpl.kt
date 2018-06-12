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
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.koinutils.Bean.inject
import straightway.peerspace.koinutils.withContext
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataPushForwarder
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.TransmissionResultListener
import straightway.random.Chooser
import straightway.utils.TimeProvider
import java.time.LocalDateTime

/**
 * Implementation of the test environment for testing the PeerImpl class.
 */
data class PeerTestEnvironmentImpl(
        override val peerId: Id,
        override val knownPeersIds: List<Id> = listOf(),
        override val unknownPeerIds: List<Id> = listOf(),
        override val localChunks: List<Chunk> = listOf(),
        private val configurationFactory: PeerTestEnvironment.() -> Configuration = {
            Configuration()
        },
        private val forwardStrategyFactory: PeerTestEnvironment.() -> ForwardStrategy = {
            mock()
        },
        private val timeProviderFactory: PeerTestEnvironment.() -> TimeProvider = {
            mock {
                on { currentTime }.thenReturn(LocalDateTime.of(2001, 1, 1, 14, 30))
            }
        },
        private val dataQueryHandlerFactory: PeerTestEnvironment.() -> DataQueryHandler = {
            mock()
        },
        private val dataPushForwarderFactory: PeerTestEnvironment.() -> DataPushForwarder = {
            mock()
        },
        private val knownPeersProviderFactory: PeerTestEnvironment.() -> KnownPeersProvider = {
            mock()
        },
        private val knownPeerQueryChooserFactory: PeerTestEnvironment.() -> Chooser = {
            createChooser { knownPeersIds }
        },
        private val knownPeerAnswerChooserFactory: PeerTestEnvironment.() -> Chooser = {
            createChooser { knownPeersIds }
        },
        private val peerDirectoryFactory: PeerTestEnvironment.() -> PeerDirectory = {
            createPeerDirectory { knownPeers }
        },
        private val networkFactory: PeerTestEnvironment.() -> Network = {
            createNetworkMock { knownPeers + unknownPeers }
        },
        private val dataChunkStoreFactory: PeerTestEnvironment.() -> DataChunkStore = {
            createChunkDataStore { localChunks }
        },
        private val peerFactory: PeerTestEnvironment.() -> Peer = {
            PeerImpl()
        }
) : PeerTestEnvironment {

    val koin by lazy {
        withContext {
            bean { configurationFactory() }
            bean { forwardStrategyFactory() }
            bean { timeProviderFactory() }
            bean { dataQueryHandlerFactory() }
            bean { dataPushForwarderFactory() }
            bean { knownPeersProviderFactory() }
            bean("knownPeerQueryChooser") { knownPeerQueryChooserFactory() }
            bean("knownPeerAnswerChooser") { knownPeerAnswerChooserFactory() }
            bean { peerDirectoryFactory() }
            bean { networkFactory() }
            bean { dataChunkStoreFactory() }
            bean { peerFactory() }
        }.apply {
            extraProperties["peerId"] = peerId.identifier
        } make { KoinModuleComponent() }
    }

    override val peer by koin.inject<Peer>()
    override val configuration: Configuration by koin.inject()
    override val knownPeerQueryChooser: Chooser by koin.inject("knownPeerQueryChooser")
    override val knownPeerAnswerChooser: Chooser by koin.inject("knownPeerAnswerChooser")
    override val timeProvider: TimeProvider by koin.inject()
    override val forwardStrategy: ForwardStrategy by koin.inject()
    override val dataPushForwarder: DataPushForwarder by koin.inject()
    override val knownPeersProvider: KnownPeersProvider by koin.inject()
    override val dataQueryHandler: DataQueryHandler by koin.inject()
    override val dataChunkStore: DataChunkStore by koin.inject()

    override val knownPeers = knownPeersIds.map {
        createPeerMock(it) { pushRequest, resultForwarder ->
            pushTransmissionResultListeners[Pair(it, pushRequest.chunk.key)] = resultForwarder
        }
    }.toMutableList()

    override val unknownPeers = unknownPeerIds.map { createPeerMock(it) }

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
