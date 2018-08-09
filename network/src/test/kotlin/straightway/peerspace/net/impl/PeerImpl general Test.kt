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

import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.Peer
import straightway.peerspace.net.KnownPeersQueryRequest
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class `PeerImpl general Test` : KoinLoggingDisabler() {

    private companion object {
        val id = Id("thePeerId")
        val knownPeersRequest = KnownPeersQueryRequest(Id("queryingPeerId"))
    }

    private val test get() = Given {
        PeerTestEnvironment(
                id,
                peerFactory = { PeerImpl() })
    }

    @Test
    fun `id is accessible`() =
            test when_ { get<Peer>().id } then { expect(it.result is_ Equal to_ peerId) }

    @Test
    fun `toString contains peer id`() =
            test when_ { get<Peer>().toString() } then {
                expect(it.result is_ Equal to_ "PeerImpl(${id.identifier})")
            }
}