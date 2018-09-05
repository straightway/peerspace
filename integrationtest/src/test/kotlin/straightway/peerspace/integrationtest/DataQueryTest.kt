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
import straightway.peerspace.data.DataQuery
import straightway.peerspace.integrationtest.SimNetwork.Companion.id
import straightway.peerspace.net.Request
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.day
import straightway.units.get

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
    fun `query is answered immediately if data is available`() =
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

    @Test
    fun `timed query is spread to all affected peers`() {
        val timedQuery = SimNetwork.dataQuery(99, 1L..2L)
        Given {
            SimNetwork {
                hash[timedQuery.withEpoch(0)] = 1
                hash[timedQuery.withEpoch(1)] = -1
                addPeer(0) {
                    knows(-1, 1)
                }
                addPeer(-1) {}
                addPeer(1) {}
            }
        } when_ {
            env(0).client.query(timedQuery.withEpoch(0)) {}
            env(0).client.query(timedQuery.withEpoch(1)) {}
            simulator.run()
        } then {
            assertSendPath(timedQuery.withEpoch(0), 0, 0, 1)
            assertSendPath(timedQuery.withEpoch(1), 0, 0, -1)
        }
    }

    @Test
    fun `timed query is split into epochs and forwarded multiple hops`() {
        lateinit var query: DataQuery
        Given {
            SimNetwork {
                query = SimNetwork.dataQuery(99, ageOf(0[day]..2[day]))
                hash[query.withEpoch(0)] = 2
                hash[query.withEpoch(1)] = -2
                addPeer(0) { knows(-1, 1) }
                addPeer(-1) { knows(0, -2) }
                addPeer(-2) { knows(0) }
                addPeer(1) { knows(0, 2) }
                addPeer(2) { knows(0) }
            }
        } when_ {
            env(0).client.query(query) {}
            simulator.run()
        } then {
            assertSendPath(query.withEpoch(0), 0, 1, 2)
            assertSendPath(query.withEpoch(1), 0, -1, -2)
        }
    }

    @Test
    fun `if no forward candidates are found, known peers are refreshed and forward is retried`() {
        Given {
            SimNetwork {
                addPeer(5) {
                    knows(9)
                }
                addPeer(9) {
                    knows(0)
                }
                addPeer(0) {}
            }
        } when_ {
            env(5).client.query(query) {}
        } then {
            assertSendPath(query, 5, 5, 0)
        }
    }
}