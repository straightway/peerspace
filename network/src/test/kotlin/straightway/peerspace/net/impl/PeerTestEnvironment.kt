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
import org.koin.core.parameter.Parameters
import org.koin.dsl.context.Context
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Bean.get
import straightway.koinutils.withContext
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataChunkStore
import straightway.peerspace.net.DataQueryHandler
import straightway.peerspace.net.ForwardStateTracker
import straightway.peerspace.net.ForwardStrategy
import straightway.peerspace.net.Forwarder
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.Network
import straightway.peerspace.net.Peer
import straightway.peerspace.net.PeerDirectory
import straightway.peerspace.net.PendingDataQueryTracker
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersGetter
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.random.Chooser
import straightway.utils.Event
import straightway.utils.TimeProvider
import java.time.LocalDateTime

typealias BeanFactory<T> = PeerTestEnvironment.() -> T

/**
 * Implementation of the test environment for testing the PeerImpl class.
 */
data class PeerTestEnvironment(
        val peerId: Id = Id("peerId"),
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
                on { now }.thenReturn(LocalDateTime.of(2001, 1, 1, 14, 30))
            }
        },
        private val dataQueryHandlerFactory: BeanFactory<DataQueryHandler> = {
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
            createNetworkMock(transmissionResultListeners) { knownPeers + unknownPeers }
        },
        private val dataChunkStoreFactory: BeanFactory<DataChunkStore> = {
            createChunkDataStore { localChunks }
        },
        private val peerFactory: BeanFactory<Peer> = {
            mock()
        },
        private val queryForwarderFactory:
        BeanFactory<Forwarder<DataQueryRequest>> = {
            mock()
        },
        private val pushForwarderFactory: BeanFactory<Forwarder<DataPushRequest>> = {
            mock()
        },
        private val pendingTimedDataQueryTrackerFactory: BeanFactory<PendingDataQueryTracker> = {
            mock()
        },
        private val pendingUntimedDataQueryTrackerFactory: BeanFactory<PendingDataQueryTracker> = {
            mock()
        },
        private val queryForwardTrackerFactory:
        BeanFactory<ForwardStateTracker<DataQueryRequest>> = {
            mock()
        },
        private val pushForwardTrackerFactory:
        BeanFactory<ForwardStateTracker<DataPushRequest>> = {
            mock()
        },
        private val dataPushTargetFactory: BeanFactory<DataPushTarget> = { mock() },
        private val dataQuerySourceFactory: BeanFactory<DataQuerySource> = { mock() },
        private val knownPeersPushTargetFactory: BeanFactory<KnownPeersPushTarget> = { mock() },
        private val knownPeersQuerySourceFactory: BeanFactory<KnownPeersQuerySource> = { mock() },
        private val knownPeersGetterFactory: BeanFactory<KnownPeersGetter> = { mock() },
        private val additionalInit: Context.() -> Unit = {}
) {

    val koin by lazy {
        withContext {
            bean { configurationFactory() }
            bean { forwardStrategyFactory() }
            bean { timeProviderFactory() }
            bean("dataQueryHandler") { dataQueryHandlerFactory() }
            bean("otherDataQueryHandler") { mock<DataQueryHandler>() }
            bean { knownPeersProviderFactory() }
            bean("knownPeerQueryChooser") { knownPeerQueryChooserFactory() }
            bean("knownPeerAnswerChooser") { knownPeerAnswerChooserFactory() }
            bean { peerDirectoryFactory() }
            bean { networkFactory() }
            bean { dataChunkStoreFactory() }
            bean { peerFactory() }
            bean("queryForwarder") { queryForwarderFactory() }
            bean("pendingTimedQueryTracker") { pendingTimedDataQueryTrackerFactory() }
            bean("pendingUntimedQueryTracker") { pendingUntimedDataQueryTrackerFactory() }
            bean("queryForwardTracker") { queryForwardTrackerFactory() }
            bean("pushForwarder") { pushForwarderFactory() }
            bean("pushForwardTracker") { pushForwardTrackerFactory() }
            bean("localDataPushTarget") { dataPushTargetFactory() }
            bean("localDataQuerySource") { dataQuerySourceFactory() }
            bean("localKnownPeersPushTarget") { knownPeersPushTargetFactory() }
            bean("localKnownPeersQuerySource") { knownPeersQuerySourceFactory() }
            bean { knownPeersGetterFactory() }
            bean("localQueryResultEvent") { Event<Chunk>() }
            additionalInit()
        }.apply {
            extraProperties["peerId"] = peerId.identifier
        } make { KoinModuleComponent() }
    }

    inline fun <reified T> get() = koin.get<T>()
    inline fun <reified T> get(beanName: String) = koin.get<T>(beanName)
    inline fun <reified T> get(noinline parameters: Parameters) = koin.get<T>(parameters)

    val knownPeers = knownPeersIds.map { createPeerMock(it) }.toMutableList()
    val transmissionResultListeners = mutableListOf<TransmissionRecord>()

    fun getPeer(id: Id) =
            knownPeers.find { it.id == id } ?: unknownPeers.find { it.id == id }!!

    private val unknownPeers = unknownPeerIds.map { createPeerMock(it) }
}