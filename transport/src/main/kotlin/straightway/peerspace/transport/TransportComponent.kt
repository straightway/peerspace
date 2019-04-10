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
package straightway.peerspace.transport

import org.koin.dsl.context.Context
import straightway.koinutils.Bean.get
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.withContext
import straightway.peerspace.crypto.CryptoFactory
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.net.PeerClient
import straightway.peerspace.transport.impl.ListQueryCallbackInstances
import straightway.utils.TimeProvider

/**
 * Base class for alltransport components that can be retrieved via koin.
 */
interface TransportComponent : KoinModuleComponent {

    companion object {
        operator fun invoke() = Impl()

        class Impl : TransportComponent, KoinModuleComponent by KoinModuleComponent()

        @Suppress("LongParameterList")
        fun createEnvironment(
                transportFactory: () -> Transport,
                peerClientFactory: () -> PeerClient,
                chunkerFactory: () -> Chunker,
                deChunkerFactory: () -> DeChunker,
                timeProviderFactory: () -> TimeProvider,
                listQueryTrackerFactory: (
                        ListQuery,
                        DeChunkerCrypto,
                        ListQueryCallback.() -> Unit) -> ListQueryCallback,
                listItemQueryTrackerFactory: (
                        DataChunk,
                        DeChunkerCrypto,
                        ListQueryCallbackInstances) -> ListItemQueryTracker,
                dataQueryTrackerFactory: (
                        Id,
                        DeChunkerCrypto,
                        DataQueryCallback.() -> Unit) -> DataQueryCallback,
                cryptoFactory: () -> CryptoFactory,
                randomBytesFactory: () -> Iterator<Byte>,
                additionalInitialization: Context.() -> Unit = {}
        ) = withContext {
            bean { transportFactory() }
            bean { peerClientFactory() }
            bean { chunkerFactory() }
            bean { deChunkerFactory() }
            bean { timeProviderFactory() }
            bean { cryptoFactory() }
            bean { randomBytesFactory() }
            factory { args ->
                listQueryTrackerFactory(args["listQuery"], args["crypto"], args["querySetup"])
            }
            factory { args ->
                listItemQueryTrackerFactory(args["initialChunk"], args["crypto"], args["callbacks"])
            }
            factory { args ->
                dataQueryTrackerFactory(args["queriedId"], args["crypto"], args["querySetup"])
            }
            additionalInitialization()
        } make { TransportComponent() }
    }
}

val TransportComponent.transport get() = get<Transport>()
val TransportComponent.peerClient get() = get<PeerClient>()
val TransportComponent.chunker get() = get<Chunker>()
val TransportComponent.deChunker get() = get<DeChunker>()
val TransportComponent.cryptoFactory get() = get<CryptoFactory>()
fun TransportComponent.createHasher() = cryptoFactory.createHasher()
val TransportComponent.timeProvider get() = get<TimeProvider>()
val TransportComponent.randomBytes get() = get<Iterator<Byte>>()

@Suppress("LongParameterList")
fun TransportComponent.createListQueryTracker(
        listQuery: ListQuery,
        crypto: DeChunkerCrypto,
        querySetup: ListQueryCallback.() -> Unit
) = get<ListQueryCallback> {
    mapOf("listQuery" to listQuery, "crypto" to crypto, "querySetup" to querySetup)
}

@Suppress("LongParameterList")
fun TransportComponent.createListItemQueryTracker(
        initialChunk: DataChunk,
        crypto: DeChunkerCrypto,
        callbacks: ListQueryCallbackInstances
) = get<ListItemQueryTracker> {
    mapOf("initialChunk" to initialChunk, "crypto" to crypto, "callbacks" to callbacks)
}

@Suppress("LongParameterList")
fun TransportComponent.createDataQueryTracker(
        queriedId: Id,
        crypto: DeChunkerCrypto,
        querySetup: DataQueryCallback.() -> Unit
) = get<DataQueryCallback> {
    mapOf("queriedId" to queriedId, "crypto" to crypto, "querySetup" to querySetup)
}