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
package straightway.peerspace.net

import org.koin.dsl.context.Context
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.withContext
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.data.Transmittable
import straightway.random.Chooser
import straightway.utils.Event
import straightway.utils.TimeProvider

@Suppress("LongParameterList")
fun createPeerEnvironment(
        peerId: Id = Id("peerId"),
        configurationFactory: () -> Configuration,
        forwardStrategyFactory: () -> ForwardStrategy,
        timeProviderFactory: () -> TimeProvider,
        dataQueryHandlerFactory: () -> DataQueryHandler,
        timedDataQueryHandlerFactory: () -> DataQueryHandler,
        untimedDataQueryHandlerFactory: () -> DataQueryHandler,
        knownPeerQueryChooserFactory: () -> Chooser,
        knownPeerAnswerChooserFactory: () -> Chooser,
        peerDirectoryFactory: () -> PeerDirectory,
        networkFactory: () -> Network,
        dataChunkStoreFactory: () -> DataChunkStore,
        peerFactory: () -> Peer,
        queryForwardTargetGetterFactory: () -> ForwardTargetGetter,
        pushForwardTargetGetterFactory: () -> ForwardTargetGetter,
        pendingTimedDataQueryTrackerFactory: () -> PendingDataQueryTracker,
        pendingUntimedDataQueryTrackerFactory: () -> PendingDataQueryTracker,
        queryForwardTrackerFactory: () -> ForwardStateTracker,
        queryForwarderFactory: () -> Forwarder,
        pushForwardTrackerFactory: () -> ForwardStateTracker,
        pushForwarderFactory: () -> Forwarder,
        dataPushTargetFactory: () -> DataPushTarget,
        dataQuerySourceFactory: () -> DataQuerySource,
        knownPeersPushTargetFactory: () -> KnownPeersPushTarget,
        knownPeersQuerySourceFactory: () -> KnownPeersQuerySource,
        knownPeersGetterFactory: () -> KnownPeersGetter,
        chunkSizeGetterFactory: () -> ChunkSizeGetter,
        localDeliveryEventFactory: () -> Event<Transmittable>,
        knownPeersReceivedEventFactory: () -> Event<KnownPeers>,
        peerClientFactory: () -> PeerClient,
        keyHasherFactory: () -> KeyHasher,
        epochAnalyzerFactory: () -> EpochAnalyzer,
        hasherFactory: () -> Hasher,
        channelFactory: (Any) -> Channel,
        additionalInit: Context.() -> Unit = {}
): KoinModuleComponent =
        withContext {
            bean { configurationFactory() }
            bean { forwardStrategyFactory() }
            bean { timeProviderFactory() }
            bean("dataQueryHandler") { dataQueryHandlerFactory() }
            bean("timedDataQueryHandler") { timedDataQueryHandlerFactory() }
            bean("untimedDataQueryHandler") { untimedDataQueryHandlerFactory() }
            bean("knownPeerQueryChooser") { knownPeerQueryChooserFactory() }
            bean("knownPeerAnswerChooser") { knownPeerAnswerChooserFactory() }
            bean { peerDirectoryFactory() }
            bean { networkFactory() }
            bean { dataChunkStoreFactory() }
            bean { peerFactory() }
            bean("queryForwarder") { queryForwarderFactory() }
            bean("queryForwardTargetGetter") { queryForwardTargetGetterFactory() }
            bean("pendingTimedQueryTracker") { pendingTimedDataQueryTrackerFactory() }
            bean("pendingUntimedQueryTracker") { pendingUntimedDataQueryTrackerFactory() }
            bean("queryForwardStateTracker") { queryForwardTrackerFactory() }
            bean("pushForwarder") { pushForwarderFactory() }
            bean("pushForwarder") { pushForwardTargetGetterFactory() }
            bean("pushForwardTargetGetter") { pushForwardTargetGetterFactory() }
            bean("pushForwardStateTracker") { pushForwardTrackerFactory() }
            bean("localDataPushTarget") { dataPushTargetFactory() }
            bean("localDataQuerySource") { dataQuerySourceFactory() }
            bean("localKnownPeersPushTarget") { knownPeersPushTargetFactory() }
            bean("localKnownPeersQuerySource") { knownPeersQuerySourceFactory() }
            bean { knownPeersGetterFactory() }
            bean { chunkSizeGetterFactory() }
            bean("localDeliveryEvent") { Event<Transmittable>() }
            bean("knownPeersReceivedEvent") { Event<KnownPeers>() }
            bean("localDeliveryEvent") { localDeliveryEventFactory() }
            bean("knownPeersReceivedEvent") { knownPeersReceivedEventFactory() }
            bean { peerClientFactory() }
            bean { keyHasherFactory() }
            bean { epochAnalyzerFactory() }
            bean { hasherFactory() }
            factory { channelFactory(it["id"]) }
            additionalInit()
        }.apply {
            extraProperties["peerId"] = peerId.identifier
        } make { KoinModuleComponent() }