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
import straightway.testing.bdd.Given

class DataPushTest : KoinLoggingDisabler() {

    companion object {
        val chunk = SimNetwork.dataChunk(0)
    }

    @Test
    fun `data is pushed over several steps to destination`() =
            Given {
                SimNetwork {
                    addPeer(9) {
                        knows(11, 5)
                    }
                    addPeer(5) {
                        knows(1, 9)
                    }
                    addPeer(1) {}
                }
            } when_ {
                client(9).store(chunk)
                simulator.run()
            } then {
                assertSendPath(chunk, 9, 5, 1)
            }

    @Test
    fun `data is pushed only to the best peers`() =
            Given {
                SimNetwork {
                    addPeer(9) {
                        knows(1, 2, 3)
                    }
                    addPeer(1) {}
                    addPeer(2) {}
                    addPeer(3) {}
                }
            } when_ {
                client(9).store(chunk)
                simulator.run()
            } then {
                assertSendPath(chunk, 9, 9, 1)
                assertSendPath(chunk, 9, 9, 2)
                assertUnaffected(3)
            }

    @Test
    fun `data is pushed not pushed to to worse peers`() =
            Given {
                SimNetwork {
                    addPeer(9) {
                        knows(11)
                    }
                    addPeer(11) {}
                }
            } when_ {
                client(9).store(chunk)
                simulator.run()
            } then {
                assertUnaffected(11)
            }
}