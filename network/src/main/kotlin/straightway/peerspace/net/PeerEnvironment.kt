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
@file:Suppress("unused")

package straightway.peerspace.net

import org.koin.dsl.context.Context
import straightway.koinutils.Bean.get
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
            bean("pushForwarderTargetGetter") { pushForwardTargetGetterFactory() }
            bean("pushForwardTargetGetter") { pushForwardTargetGetterFactory() }
            bean("pushForwardStateTracker") { pushForwardTrackerFactory() }
            bean("localDataPushTarget") { dataPushTargetFactory() }
            bean("localDataQuerySource") { dataQuerySourceFactory() }
            bean("localKnownPeersPushTarget") { knownPeersPushTargetFactory() }
            bean("localKnownPeersQuerySource") { knownPeersQuerySourceFactory() }
            bean { knownPeersGetterFactory() }
            bean { chunkSizeGetterFactory() }
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

val KoinModuleComponent.configuration: Configuration get() =
    get()
val KoinModuleComponent.forwardStrategy: ForwardStrategy get() =
    get()
val KoinModuleComponent.timeProvider: TimeProvider get() =
    get()
val KoinModuleComponent.dataQueryHandler: DataQueryHandler get() =
    get("dataQueryHandler")
val KoinModuleComponent.timedDataQueryHandler: DataQueryHandler get() =
    get("timedDataQueryHandler")
val KoinModuleComponent.untimedDataQueryHandler: DataQueryHandler get() =
    get("untimedDataQueryHandler")
val KoinModuleComponent.knownPeerQueryChooser: Chooser get() =
    get("knownPeerQueryChooser")
val KoinModuleComponent.knownPeerAnswerChooser: Chooser get() =
    get("knownPeerAnswerChooser")
val KoinModuleComponent.peerDirectory: PeerDirectory get() =
    get()
val KoinModuleComponent.network: Network get() =
    get()
val KoinModuleComponent.dataChunkStore: DataChunkStore get() =
    get()
val KoinModuleComponent.peer: Peer get() =
    get()
val KoinModuleComponent.queryForwardTargetGetter: ForwardTargetGetter get() =
    get("queryForwardTargetGetter")
val KoinModuleComponent.pushForwardTargetGetter: ForwardTargetGetter get() =
    get("pushForwarderTargetGetter")
val KoinModuleComponent.pendingTimedDataQueryTracker: PendingDataQueryTracker get() =
    get("pendingTimedQueryTracker")
val KoinModuleComponent.pendingUntimedDataQueryTracker: PendingDataQueryTracker get() =
    get("pendingUntimedQueryTracker")
val KoinModuleComponent.queryForwardStateTracker: ForwardStateTracker get() =
    get("queryForwardStateTracker")
val KoinModuleComponent.queryForwarder: Forwarder get() =
    get("queryForwarder")
val KoinModuleComponent.pushForwardStateTracker: ForwardStateTracker get() =
    get("pushForwardStateTracker")
val KoinModuleComponent.pushForwarder: Forwarder get() =
    get("pushForwarder")
val KoinModuleComponent.dataPushTarget: DataPushTarget get() =
    get("localDataPushTarget")
val KoinModuleComponent.dataQuerySource: DataQuerySource get() =
    get("localDataQuerySource")
val KoinModuleComponent.knownPeersPushTarget: KnownPeersPushTarget get() =
    get("localKnownPeersPushTarget")
val KoinModuleComponent.knownPeersQuerySource: KnownPeersQuerySource get() =
    get("localKnownPeersQuerySource")
val KoinModuleComponent.knownPeersGetter: KnownPeersGetter get() =
    get()
val KoinModuleComponent.chunkSizeGetter: ChunkSizeGetter get() =
    get()
val KoinModuleComponent.localDeliveryEvent: Event<Transmittable> get() =
    get("localDeliveryEvent")
val KoinModuleComponent.knownPeersReceivedEvent: Event<KnownPeers> get() =
    get("knownPeersReceivedEvent")
val KoinModuleComponent.peerClient: PeerClient get() =
    get()
val KoinModuleComponent.keyHasher: KeyHasher get() =
    get()
val KoinModuleComponent.epochAnalyzer: EpochAnalyzer get() =
    get()
val KoinModuleComponent.hasher: Hasher get() =
    get()
fun KoinModuleComponent.createChannelTo(remotePeerId: Id): Channel =
        get { mapOf("id" to remotePeerId) }