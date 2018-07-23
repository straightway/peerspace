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

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.ForwardState
import straightway.peerspace.net.impl.ForwardStrategyTestEnvironment.Companion.chunkKey
import straightway.peerspace.net.impl.ForwardStrategyTestEnvironment.Companion.chunkKeyHash
import straightway.peerspace.net.impl.ForwardStrategyTestEnvironment.Companion.idForHash
import straightway.peerspace.net.impl.ForwardStrategyTestEnvironment.Companion.peerId
import straightway.testing.bdd.Given
import straightway.testing.flow.Empty
import straightway.testing.flow.Equal
import straightway.testing.flow.Values
 import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class `ForwardStrategyImplTest getForwardPeerIdsFor ChunkKeys` : KoinLoggingDisabler() {

    private val test get() = Given { ForwardStrategyTestEnvironment() }

    @Test
    fun `no forward peers empty result if no peer is known`() =
            test while_ {
                knownPeerIds.clear()
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `single forward peer id if it is nearer to the data than local peer`() =
            test while_ {
                addKnownPeer(idForHash[chunkKeyHash]!!, chunkKeyHash)
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                expect(it.result is_ Equal to_ Values(idForHash[chunkKeyHash]!!))
            }

    @Test
    fun `no forward peers if local peer is nearest to the data`() =
            test while_ {
                addKnownPeer(idForHash[-100]!!, -100)
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `forward to nearer peer on the other side of the data`() =
            test while_ {
                addKnownPeer(idForHash[250]!!, 250)
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                expect(it.result is_ Empty)
            }

    @Test
    fun `nearer peer on the other side of the data 2`() =
            test while_ {
                hashes[Key(peerId)] = listOf(300L)
                addKnownPeer(idForHash[chunkKeyHash]!!, chunkKeyHash)
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                expect(it.result is_ Equal to_ Values(idForHash[chunkKeyHash]!!))
            }

    @Test
    fun `use chooser to choose peers`() =
            test while_ {
                addKnownPeerForHash(50)
                addKnownPeerForHash(100)
                addKnownPeerForHash(150)
                chosenForwardPeers = listOf(idForHash[100]!!, idForHash[150]!!)
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                expect(it.result is_ Equal to_ chosenForwardPeers!!.toSet())
            }

    @Test
    fun `configured number of peers is passed to chooser`() =
            test when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                verify(forwardPeerChooser).chooseFrom(
                        any<List<Id>>(),
                        eq(configuration.numberOfForwardPeers))
            }

    @Test
    fun `all nearer peers are passed to chooser`() =
            test while_ {
                addKnownPeerForHash(-100)
                addKnownPeerForHash(50)
                addKnownPeerForHash(100)
                addKnownPeerForHash(150)
                chosenForwardPeers = listOf(idForHash[100]!!, idForHash[150]!!)
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, ForwardState())
            } then {
                verify(forwardPeerChooser).chooseFrom(
                        eq(listOf(idForHash[50]!!, idForHash[100]!!, idForHash[150]!!)),
                        any())
            }

    @Test
    fun `failed transmission is filled up while other is pending`() =
            test while_ {
                addKnownPeerForHash(50)
                addKnownPeerForHash(100)
                addKnownPeerForHash(150)
                forwardState = forwardState.setPending(idForHash[100]!!)
                forwardState = forwardState.setFailed(idForHash[50]!!)
                forwardState = forwardState.setFailed(Id("otherFailedPeerId"))
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, forwardState)
            } then {
                verify(forwardPeerChooser).chooseFrom(listOf(idForHash[150]!!), 1)
            }

    @Test
    fun `failed transmission is filled up while other succeeded`() =
            test while_ {
                addKnownPeerForHash(50)
                addKnownPeerForHash(100)
                addKnownPeerForHash(150)
                forwardState = forwardState.setSuccess(idForHash[100]!!)
                forwardState = forwardState.setFailed(idForHash[50]!!)
                forwardState = forwardState.setFailed(Id("otherFailedPeerId"))
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, forwardState)
            } then {
                verify(forwardPeerChooser).chooseFrom(listOf(idForHash[150]!!), 1)
            }

    @Test
    fun `failed transmission is ignored if enough unfailed transmissions exist`() =
            test while_ {
                addKnownPeerForHash(50)
                addKnownPeerForHash(100)
                addKnownPeerForHash(150)
                forwardState = forwardState.setSuccess(idForHash[100]!!)
                forwardState = forwardState.setSuccess(idForHash[50]!!)
                forwardState = forwardState.setFailed(Id("otherFailedPeerId"))
            } when_ {
                sut.getForwardPeerIdsFor(chunkKey, forwardState)
            } then {
                expect(it.result is_ Empty)
                verify(forwardPeerChooser, never()).chooseFrom<Id>(any(), any())
            }
}