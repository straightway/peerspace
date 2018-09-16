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
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.getProperty
import straightway.koinutils.withContext
import straightway.peerspace.crypto.Hasher
import straightway.peerspace.data.Id
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.data.Transmittable
import straightway.random.Chooser
import straightway.utils.Event
import straightway.utils.TimeProvider

/**
 * Base interface for classes who's implementation needs access
 * to the defined koin components of a peer.
 */
interface PeerComponent : KoinModuleComponent {

    @Suppress("LargeClass")
    companion object {
        operator fun invoke() = Impl()

        class Impl : PeerComponent, KoinModuleComponent by KoinModuleComponent()

        @Suppress("LongParameterList")
        fun createEnvironment(
                peerId: Id,
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
        ) =
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
                } make { PeerComponent() }
    }
}

val PeerComponent.localPeerId: Id get() =
    Id(getProperty("peerId"))
val PeerComponent.configuration: Configuration get() =
    get()
val PeerComponent.forwardStrategy: ForwardStrategy get() =
    get()
val PeerComponent.timeProvider: TimeProvider get() =
    get()
val PeerComponent.dataQueryHandler: DataQueryHandler get() =
    get("dataQueryHandler")
val PeerComponent.timedDataQueryHandler: DataQueryHandler get() =
    get("timedDataQueryHandler")
val PeerComponent.untimedDataQueryHandler: DataQueryHandler get() =
    get("untimedDataQueryHandler")
val PeerComponent.knownPeerQueryChooser: Chooser get() =
    get("knownPeerQueryChooser")
val PeerComponent.knownPeerAnswerChooser: Chooser get() =
    get("knownPeerAnswerChooser")
val PeerComponent.peerDirectory: PeerDirectory get() =
    get()
val PeerComponent.network: Network get() =
    get()
val PeerComponent.dataChunkStore: DataChunkStore get() =
    get()
val PeerComponent.peer: Peer get() =
    get()
val PeerComponent.queryForwardTargetGetter: ForwardTargetGetter get() =
    get("queryForwardTargetGetter")
val PeerComponent.pushForwardTargetGetter: ForwardTargetGetter get() =
    get("pushForwarderTargetGetter")
val PeerComponent.pendingTimedDataQueryTracker: PendingDataQueryTracker get() =
    get("pendingTimedQueryTracker")
val PeerComponent.pendingUntimedDataQueryTracker: PendingDataQueryTracker get() =
    get("pendingUntimedQueryTracker")
val PeerComponent.queryForwardStateTracker: ForwardStateTracker get() =
    get("queryForwardStateTracker")
val PeerComponent.queryForwarder: Forwarder get() =
    get("queryForwarder")
val PeerComponent.pushForwardStateTracker: ForwardStateTracker get() =
    get("pushForwardStateTracker")
val PeerComponent.pushForwarder: Forwarder get() =
    get("pushForwarder")
val PeerComponent.dataPushTarget: DataPushTarget get() =
    get("localDataPushTarget")
val PeerComponent.dataQuerySource: DataQuerySource get() =
    get("localDataQuerySource")
val PeerComponent.knownPeersPushTarget: KnownPeersPushTarget get() =
    get("localKnownPeersPushTarget")
val PeerComponent.knownPeersQuerySource: KnownPeersQuerySource get() =
    get("localKnownPeersQuerySource")
val PeerComponent.knownPeersGetter: KnownPeersGetter get() =
    get()
val PeerComponent.chunkSizeGetter: ChunkSizeGetter get() =
    get()
val PeerComponent.localDeliveryEvent: Event<Transmittable> get() =
    get("localDeliveryEvent")
val PeerComponent.knownPeersReceivedEvent: Event<KnownPeers> get() =
    get("knownPeersReceivedEvent")
val PeerComponent.peerClient: PeerClient get() =
    get()
val PeerComponent.keyHasher: KeyHasher get() =
    get()
val PeerComponent.epochAnalyzer: EpochAnalyzer get() =
    get()
val PeerComponent.hasher: Hasher get() =
    get()
fun PeerComponent.createChannelTo(remotePeerId: Id): Channel =
    get { mapOf("id" to remotePeerId) }