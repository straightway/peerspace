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

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.data.Id
import straightway.peerspace.net.KnownPeersProvider
import straightway.peerspace.net.KnownPeersQueryRequest
import straightway.peerspace.net.KnownPeersQuerySource
import straightway.peerspace.net.TransmissionResultListener
import straightway.testing.bdd.Given

class KnownPeersQuerySourceImplTest : KoinLoggingDisabler() {

    private companion object {
        val originatorId = Id("originator")
        val query = KnownPeersQueryRequest(originatorId)
    }

    private val test get() =
            Given {
                PeerTestEnvironment(
                        knownPeersQuerySourceFactory = { KnownPeersQuerySourceImpl() }
                )
            }

    @Test
    fun `query is forwarded to KnownPeersProvider`() =
            test when_ {
                get<KnownPeersQuerySource>().query(query)
            } then {
                verify(get<KnownPeersProvider>()).pushKnownPeersTo(query.originatorId)
            }

    @Test
    fun `query signals success`() {
        val listener: TransmissionResultListener = mock()
        test when_ {
            get<KnownPeersQuerySource>().query(query, listener)
        } then {
            verify(listener).notifySuccess()
        }
    }
}