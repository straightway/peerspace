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
import org.koin.dsl.context.Context
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.koinutils.KoinModuleComponent
import straightway.peerspace.koinutils.Bean.get
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
import straightway.peerspace.net.QueryRequest
import straightway.peerspace.net.TransmissionResultListener
import straightway.random.Chooser
import straightway.utils.TimeProvider
import java.time.LocalDateTime

typealias BeanFactory<T> = PeerTestEnvironment.() -> T

/**
 * Implementation of the test environment for testing the PeerImpl class.
 */
data class PeerTestEnvironment(
        val peerId: Id,
        val knownPeersIds: List<Id> = listOf(),
        val unknownPeerIds: List<Id> = listOf(),
        val localChunks: List<Chunk> = listOf(),
        private val configurationFactory: BeanFactory<Configuration> = {
            Configuration()
        },
        private val forwardStrategyFactory: BeanFactory<ForwardStrategy> = {
            mock()
        },
        private val timeProviderFactory: BeanFactory<TimeProvider> = {
            mock {
                on { currentTime }.thenReturn(LocalDateTime.of(2001, 1, 1, 14, 30))
            }
        },
        private val dataQueryHandlerFactory: BeanFactory<DataQueryHandler> = {
            mock()
        },
        private val dataPushForwarderFactory: BeanFactory<DataPushForwarder> = {
            mock()
        },
        private val knownPeersProviderFactory: BeanFactory<KnownPeersProvider> = {
            mock()
        },
        private val knownPeerQueryChooserFactory: BeanFactory<Chooser> = {
            createChooser { knownPeersIds }
        },
        private val knownPeerAnswerChooserFactory: BeanFactory<Chooser> = {
            createChooser { knownPeersIds }
        },
        private val peerDirectoryFactory: BeanFactory<PeerDirectory> = {
            createPeerDirectory { knownPeers }
        },
        private val networkFactory: BeanFactory<Network> = {
            createNetworkMock { knownPeers + unknownPeers }
        },
        private val dataChunkStoreFactory: BeanFactory<DataChunkStore> = {
            createChunkDataStore { localChunks }
        },
        private val peerFactory: BeanFactory<Peer> = {
            mock()
        },
        private val additionalInit: Context.() -> Unit = {}
) {

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
            additionalInit()
        }.apply {
            extraProperties["peerId"] = peerId.identifier
        } make { KoinModuleComponent() }
    }

    inline fun <reified T> get() = koin.get<T>()

    val knownPeers = knownPeersIds.map {
        createPeerMock(
                it,
                pushCallback = { pushRequest, resultForwarder ->
                    pushTransmissionResultListeners[Pair(it, pushRequest.chunk.key)] =
                            resultForwarder
                },
                queryCallback = { queryRequest, resultForwarder ->
                    queryTransmissionResultListeners[Pair(it, queryRequest)] =
                            resultForwarder
                })
    }.toMutableList()

    val unknownPeers = unknownPeerIds.map { createPeerMock(it) }

    fun setPeerPushSuccess(id: Id, success: Boolean) {
        peerPushSuccess[id] = success
    }

    val queryTransmissionResultListeners =
            mutableMapOf<Pair<Id, QueryRequest>, TransmissionResultListener>()

    val pushTransmissionResultListeners =
            mutableMapOf<Pair<Id, Key>, TransmissionResultListener>()

    fun getPeer(id: Id) =
            knownPeers.find { it.id == id } ?: unknownPeers.find { it.id == id }!!

    private val peerPushSuccess = mutableMapOf<Id, Boolean>()
}