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

import org.junit.jupiter.api.Test
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.integrationtest.SimNetwork.Companion.id
import straightway.peerspace.net.Request
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class DataQueryTest : KoinLoggingDisabler() {

    private companion object {
        val query = SimNetwork.dataQuery(0)
        val chunk = SimNetwork.dataChunk(0)
    }

    @Test
    fun `chunk is not forwarded twice due to query and forward strategy`() =
            Given {
                SimNetwork {
                    addPeer(2) { // queried
                        knows(1)
                    }
                    addPeer(1) {} // queryer
                }
            } when_ {
                peer(1).pushDataChunk(Request(id(2), chunk))
                peer(1).queryData(Request(id(2), query))
                simulator.run()
            } then { _ ->
                expect(log.filter { it.request.remotePeerId == id(1) }.size is_ Equal to_ 1)
            }

    @Test
    fun `query is answered immediately if data is avalilabe`() =
            Given {
                SimNetwork {
                    addPeer(2) { // queryier
                        knows(1)
                    }
                    addPeer(1) {
                        holdsData(chunk)
                    } // queried
                }
            } when_ {
                client(2).query(query) {}
                simulator.run()
            } then {
                assertSendPath(query, 2, 1)
                assertSendPath(chunk, 1, 2)
            }

    @Test
    fun `query is answered as soon as data comes in`() =
            Given {
                SimNetwork {
                    addPeer(3) { // queryier
                        knows(1)
                    }
                    addPeer(2) { // pusher
                        knows(1)
                    }
                    addPeer(1) {} // queried
                }
            } when_ {
                client(3).query(query) {}
                simulator.run()
                client(2).store(chunk)
                simulator.run()
            } then {
                assertSendPath(query, 3, 1)
                assertSendPath(chunk, 2, 1, 3)
            }
}