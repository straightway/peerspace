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
package straightway.peerspace.integrationtest

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.DataQuery
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.KeyHashable
import straightway.peerspace.data.KeyHasher
import straightway.peerspace.data.Transmittable
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.Peer
import straightway.peerspace.net.Request
import straightway.peerspace.net.handle
import straightway.peerspace.net.impl.DataPushTargetImpl
import straightway.peerspace.net.impl.DataQuerySourceImpl
import straightway.peerspace.net.impl.KnownPeersPushTargetImpl
import straightway.peerspace.net.impl.KnownPeersQuerySourceImpl
import straightway.peerspace.net.impl.PeerImpl
import straightway.peerspace.networksimulator.SimNode
import straightway.sim.core.Simulator
import straightway.testing.flow.expect
import straightway.units.Time
import straightway.units.UnitValue
import straightway.units.get
import straightway.units.milli
import straightway.units.minus
import straightway.units.second
import straightway.units.unitValue
import straightway.units.year

interface PeerBuilder {
    val id: Id
    fun knows(vararg otherPeerIds: Int)
    fun holdsData(chunk: DataChunk)
}

interface HashSetter {
    operator fun set(hashable: KeyHashable, newHashes: List<Number>)
}

operator fun HashSetter.set(hashable: KeyHashable, newHash: Number) {
    this[hashable] = listOf(newHash.toLong())
}

class SimNetwork(
        val simulator: Simulator = Simulator(),
        private val peerFactory: () -> Peer = { PeerImpl() },
        initializer: SimNetwork.() -> Unit) {

    companion object {
        fun id(id: Int) = Id("#$id")
        fun key(id: Int) = Key(id(id))
        fun key(id: Int, timestamp: Long) = Key(id(id), timestamp)
        fun dataChunk(id: Int) = DataChunk(key(id), noData)
        fun dataChunk(id: Int, timestamp: Long) = DataChunk(key(id, timestamp), noData)
        fun dataQuery(id: Int) = DataQuery(id(id))
        fun dataQuery(id: Int, timestamps: ClosedRange<Long>) = DataQuery(id(id), timestamps)
        private val noData = byteArrayOf()
    }

    private val simNodes = mutableMapOf<Any, SimNode>()
    private val peerEnvironments = mutableMapOf<Id, SinglePeerEnvironment>()
    private val hashes = mutableMapOf<Any, List<Long>>()

    data class RequestLogEntry(val receiver: Id, val request: Request<*>) {
        override fun toString() =
                "${request.remotePeerId.identifier} --${request.content}--> ${receiver.identifier}"
    }

    val log: List<RequestLogEntry> get() = _log

    fun env(id: Id) = peerEnvironments[id]!!
    fun env(id: Int) = env(id(id))

    fun peer(id: Int) = env(id).peer
    fun client(id: Int) = env(id).client

    fun addPeer(id: Int, initialize: (PeerBuilder.() -> Unit)) {
        val peerEnvironment = createPeerEnvironment(id, initialize)
        val node = peerEnvironment.node
        peerEnvironments.values.forEach {
            peerEnvironment.addRemoteNode(it.node)
            it.addRemoteNode(node)
        }
        peerEnvironments[id(id)] = peerEnvironment
    }

    fun assertSendPath(item: Transmittable, vararg path: Int) =
            SendPathChecker(item, path).assertSendPath()

    fun assertUnaffected(id: Int) {
        val peer = env(id).peer
        verify(peer, never()).queryData(any())
        verify(peer, never()).pushDataChunk(any())
    }

    val hash = object : HashSetter {
        override fun set(hashable: KeyHashable, newHashes: List<Number>) {
            hashes[hashable] = newHashes.map { it.toLong() }
        }
    }

    fun ageOf(time: UnitValue<Time>) =
            (simulator.now - time).unitValue[milli(second)].value.toLong()
    fun ageOf(range: ClosedRange<UnitValue<Time>>) =
            ageOf(range.start)..ageOf(range.endInclusive)

    init {
        simulator.schedule(2013.5[year], "Set initial time") {}
        simulator.run()
        this.initializer()
    }

    private fun createPeerEnvironment(
            id: Int,
            initialize: PeerBuilder.() -> Unit
    ): SinglePeerEnvironment {
        val peerBuilder = PeerBuilderImpl(id)
        hashes[id(id)] = listOf(id.toLong())
        peerBuilder.initialize()
        val peerEnvironment = peerBuilder.create()
        return peerEnvironment
    }

    @Suppress("SwallowedException")
    private val KeyHashable.encodedHash get(): Long? {
        val idId = id as? Id
        return if (idId != null)
            try {
                idId.identifier.substring(1).toLong() + if (epoch == null) 0 else epoch!! * 10000
            } catch (x: NumberFormatException) {
                null
            }
        else null
    }

    private fun wrapMockPeer(wrapped: Peer) =
            mock<Peer> { _ ->
                on { id }.thenAnswer {
                    wrapped.id
                }
                on { pushDataChunk(any()) }.thenAnswer {
                    wrapped.pushDataChunk(it.getArgument(0))
                }
                on { queryData(any()) }.thenAnswer {
                    wrapped.queryData(it.getArgument(0))
                }
                on { pushKnownPeers(any()) }.thenAnswer {
                    wrapped.pushKnownPeers(it.getArgument(0))
                }
                on { queryKnownPeers(any()) }.thenAnswer {
                    wrapped.queryKnownPeers(it.getArgument(0))
                }
                on { toString() }.thenAnswer { wrapped.toString() }
            }

    private fun wrapLoggingMock(id: Id, wrapped: DataPushTarget) =
            mock<DataPushTarget> { _ ->
                on { pushDataChunk(any()) }.thenAnswer {
                    wrapped.handleLogging(id, it.getArgument(0))
                }
                on { toString() }.thenAnswer { wrapped.toString() }
            }

    private fun wrapLoggingMock(id: Id, wrapped: DataQuerySource) =
            mock<DataQuerySource> { _ ->
                on { queryData(any()) }.thenAnswer {
                    wrapped.handleLogging(id, it.getArgument(0))
                }
                on { toString() }.thenAnswer { wrapped.toString() }
            }

    private fun wrapLoggingMock(id: Id, wrapped: KnownPeersPushTarget) =
            mock<KnownPeersPushTarget> { _ ->
                on { pushKnownPeers(any()) }.thenAnswer {
                    wrapped.handleLogging(id, it.getArgument(0))
                }
                on { toString() }.thenAnswer { wrapped.toString() }
            }

    private fun wrapLoggingMock(id: Id, wrapped: KnownPeersQuerySource) =
            mock<KnownPeersQuerySource> { _ ->
                on { queryKnownPeers(any()) }.thenAnswer {
                    wrapped.handleLogging(id, it.getArgument(0))
                }
                on { toString() }.thenAnswer { wrapped.toString() }
            }

    private fun Any.handleLogging(sourceId: Id, request: Request<*>) {
        _log.add(RequestLogEntry(sourceId, request))
        handle(request)
    }

    private val _log = mutableListOf<RequestLogEntry>()

    private inner class SimKeyHasher : KeyHasher {
        override fun getHashes(hashable: KeyHashable): Iterable<Long> =
                hashes[hashable] ?: listOf(hashable.encodedHash ?: 0)
    }

    private inner class SendPathChecker(val item: Transmittable, path: IntArray) {
        private var currSender = path.firstOrNull()
        private var restPath = path.drop(1)
        private var restLog = _log.toList()

        fun assertSendPath() {
            while (currSender != null && restPath.any()) {
                assertSendPathHop()
                currSender = restPath.first()
                restPath = restPath.drop(1)
            }
        }

        private fun assertSendPathHop() {
            val newRestLog = restLog.dropWhile { !it.isMatchingCurrentHop }
            expect(newRestLog.any()) {
                "Could not find " +
                "${id(currSender!!).identifier} --$item--> ${currReceiver.identifier}"
            }
            restLog = newRestLog.drop(1)
        }

        private val RequestLogEntry.isMatchingCurrentHop get() =
            request.remotePeerId == id(currSender!!) &&
            receiver == currReceiver &&
            request.content == item

        private val currReceiver get() = id(restPath.first())
    }

    private inner class PeerBuilderImpl(id: Int) : PeerBuilder {
        override val id: Id = id(id)
        override fun knows(vararg otherPeerIds: Int) {
            knownPeers.addAll(otherPeerIds.map { id(it) })
        }
        override fun holdsData(chunk: DataChunk) {
            heldData.add(chunk)
        }
        fun create() = SinglePeerEnvironment(
                peerId = this.id,
                simulator = simulator,
                peerFactory = { wrapMockPeer(peerFactory()) },
                dataPushTargetFactory = {
                    wrapLoggingMock(this.id, DataPushTargetImpl())
                },
                dataQuerySourceFactory = {
                    wrapLoggingMock(this.id, DataQuerySourceImpl())
                },
                knownPeersPushTargetFactory = {
                    wrapLoggingMock(this.id, KnownPeersPushTargetImpl())
                },
                knownPeersQuerySourceFactory = {
                    wrapLoggingMock(this.id, KnownPeersQuerySourceImpl())
                },
                simNodes = simNodes,
                keyHasherFactory = { SimKeyHasher() }
        ).apply {
            knownPeers.forEach { addKnownPeer(it) }
            heldData.forEach { addData(it) }
        }
        val knownPeers = mutableSetOf<Id>()
        val heldData = mutableListOf<DataChunk>()
    }
}