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
package straightway.peerspace.networksimulator

import org.junit.jupiter.api.Test
import straightway.peerspace.data.Id
import straightway.peerspace.koinutils.KoinLoggingDisabler
import straightway.peerspace.koinutils.withContext
import straightway.testing.bdd.Given
import straightway.testing.flow.References
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.has
import straightway.testing.flow.is_

class SimNodeTest : KoinLoggingDisabler() {

    private companion object {
        val id = Id("id")
    }

    private val test get() = Given {
        object {
            val existingInstances = mutableMapOf<Id, SimNode>()
            fun createSimNode() =
                    withContext {
                        bean("simNodes") { existingInstances }
                    }.apply {
                        extraProperties["peerId"] = id.identifier
                    } make {
                        SimNode()
                    }
        }
    }

    @Test
    fun `construction adds to existing instances`() =
            test when_ {
                createSimNode()
            } then {
                expect(existingInstances.values has References(it.result))
            }

    @Test
    fun `construction adds to existing instances under id`() =
            test when_ {
                createSimNode()
            } then {
                expect(existingInstances[id] is_ Same as_ it.result)
            }
}