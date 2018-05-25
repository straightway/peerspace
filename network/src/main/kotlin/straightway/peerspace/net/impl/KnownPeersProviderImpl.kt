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

import straightway.peerspace.data.Chunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.net.Administrative
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.InfrastructureProvider
import straightway.peerspace.net.InfrastructureReceiver
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.PushRequest
import straightway.random.Chooser
import straightway.utils.serializeToByteArray

/**
 * Push known peers to a target peer.
 */
class KnownPeersProviderImpl(private val id: Id)
    : KnownPeersProvider, InfrastructureProvider, InfrastructureReceiver {

    override lateinit var infrastructure: Infrastructure

    override fun pushKnownPeersTo(targetPeerId: Id) =
            targetPeerId.asPushTarget.push(knownPeersAnswerRequest)

    private val knownPeersAnswerRequest
        get() = PushRequest(id, Chunk(knownPeersChunkKey, serializedKnownPeersQueryAnswer))

    private val serializedKnownPeersQueryAnswer
        get() = knownPeersQueryAnswer.serializeToByteArray()

    private val knownPeersQueryAnswer
        get() = knownPeerAnswerChooser choosePeers configuration.maxKnownPeersAnswers

    private infix fun Chooser.choosePeers(number: Int) =
            chooseFrom(allKnownPeersIds, number)

    private val allKnownPeersIds
        get() = peerDirectory.allKnownPeersIds.toList()

    private val Id.asPushTarget
        get() = getPushTargetFor(this)

    private companion object {
        val knownPeersChunkKey = Key(Administrative.KnownPeers.id)
    }
}