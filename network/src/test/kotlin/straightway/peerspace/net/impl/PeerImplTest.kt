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

import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Key
import straightway.peerspace.net.DataPushRequest
import straightway.peerspace.net.DataPushTarget
import straightway.peerspace.net.DataQueryRequest
import straightway.peerspace.net.DataQuerySource
import straightway.peerspace.net.KnownPeersPushRequest
import straightway.peerspace.net.KnownPeersPushTarget
import straightway.peerspace.net.KnownPeersQueryRequest
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.Peer
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class PeerImplTest : KoinLoggingDisabler() {

    private companion object {
        val id = Id("thePeerId")
    }

    private val test get() = Given {
        PeerTestEnvironment(
                id,
                peerFactory = { PeerImpl() })
    }

    @Test
    fun `id is accessible`() =
            test when_ { sut.id } then { expect(it.result is_ Equal to_ peerId) }

    @Test
    fun `toString contains peer id`() =
            test when_ { sut.toString() } then {
                expect(it.result is_ Equal to_ "PeerImpl(${id.identifier})")
            }

    @Test
    fun `handle with DataPushRequest calls according implementation`() {
        val request = DataPushRequest(
                Id("originator"),
                Chunk(Key(Id("Chunk")), byteArrayOf()))
        test when_ {
            sut.handle(request)
        } then {
            verify(get<DataPushTarget>()).push(request)
        }
    }

    @Test
    fun `handle with DataQueryRequest calls according implementation`() {
        val request = DataQueryRequest(Id("originator"), Id("chunk"))
        test when_ {
            sut.handle(request)
        } then {
            verify(get<DataQuerySource>()).query(request)
        }
    }

    @Test
    fun `handle with KnownPeersPushRequest calls according implementation`() {
        val request = KnownPeersPushRequest(Id("originator"), listOf())
        test when_ {
            sut.handle(request)
        } then {
            verify(get<KnownPeersPushTarget>()).push(request)
        }
    }

    @Test
    fun `handle with KnownPeersQueryRequest calls according implementation`() {
        val request = KnownPeersQueryRequest(Id("originator"))
        test when_ {
            sut.handle(request)
        } then {
            verify(get<KnownPeersQuerySource>()).query(request)
        }
    }

    private val PeerTestEnvironment.sut get() = get<Peer>()
}