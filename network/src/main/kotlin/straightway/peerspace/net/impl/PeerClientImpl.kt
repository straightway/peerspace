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

import straightway.koinutils.Bean.inject
import straightway.koinutils.KoinModuleComponent
import straightway.koinutils.Property.property
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Transmittable
import straightway.peerspace.data.isMatching
import straightway.peerspace.data.isUntimed
import straightway.peerspace.net.Configuration
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.PeerClient
import straightway.peerspace.net.QueryControl
import straightway.peerspace.net.Request
import straightway.units.toDuration
import straightway.utils.Event
import straightway.utils.EventHandlerToken
import straightway.utils.TimeProvider
import java.time.LocalDateTime

/**
 * Default implementation of the PeerClient interface.
 */
class PeerClientImpl : PeerClient, KoinModuleComponent by KoinModuleComponent() {

    private val peerId: Id by property("peerId") { Id(it) }
    private val querySource: DataQuerySource by inject()
    private val pushTarget: DataPushTarget by inject()
    private val localDeliveryEvent: Event<Transmittable> by inject("localDeliveryEvent")
    private val timeProvider: TimeProvider by inject()
    private val configuration: Configuration by inject()

    override fun store(data: DataChunk) {
        removeExpiredPendingQueries()
        pushTarget.pushDataChunk(Request(peerId, data))
    }

    override fun query(
            query: DataQuery,
            receiveCallback: QueryControl.(DataChunk) -> Unit
    ): QueryControl {
        removeExpiredPendingQueries()
        return PendingQuery(query, receiveCallback).also { pendingQueries += it }
    }

    val numberOfPendingQueries get() = pendingQueries.size

    private fun removeExpiredPendingQueries() =
            pendingQueries.forEach { it.checkExpiration() }

    private var pendingQueries = listOf<PendingQuery>()

    private inner class PendingQuery(
            val query: DataQuery,
            val receiveCallback: QueryControl.(DataChunk) -> Unit
    ) : QueryControl {

        private val eventHandlerToken: EventHandlerToken
        private lateinit var expirationTime: LocalDateTime
        private var expirationCallbacks = listOf<QueryControl.(DataQuery) -> Unit>()

        init {
            keepAlive()
            eventHandlerToken = localDeliveryEvent.attach { transmittable ->
                removeExpiredPendingQueries()
                handleDataArrival(transmittable)
            }
        }

        override fun stopReceiving() {
            localDeliveryEvent.detach(eventHandlerToken)
            pendingQueries -= this
        }

        override fun keepAlive() {
            expirationTime =
                    timeProvider.now + configuration.timedDataQueryTimeout.toDuration()
            querySource.queryData(Request(peerId, query))
        }

        override fun onExpiring(callback: QueryControl.(DataQuery) -> Unit) {
            expirationCallbacks += callback
        }

        fun checkExpiration() {
            if (isExpired) {
                notifyExpiration()
            }
            if (isExpired) { // expiration may have changed in notifyExpiration call
                stopReceiving()
            }
        }

        private val isExpired get() = expirationTime <= timeProvider.now

        private fun notifyExpiration() =
                expirationCallbacks.forEach { it(query) }

        private val handleDataArrival: (Transmittable) -> Unit =
                if (query.isUntimed) { it -> handleDataArrivalForUntimedQuery(it) }
                else { it -> handleDataArrivalForTimedQuery(it) }

        private fun handleDataArrivalForUntimedQuery(data: Transmittable) {
            localDeliveryEvent.detach(eventHandlerToken)
            forwardNotificationAboutArrived(data)
        }

        private fun handleDataArrivalForTimedQuery(data: Transmittable) {
            if (timeProvider.now < expirationTime)
                forwardNotificationAboutArrived(data)
        }

        private fun forwardNotificationAboutArrived(data: Transmittable) {
            if (data is DataChunk && query.isMatching(data.key))
                receiveCallback(data)
        }
    }
}