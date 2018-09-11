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
import straightway.expr.minus
import straightway.peerspace.data.Id
import straightway.koinutils.KoinLoggingDisabler
import straightway.peerspace.net.ForwardState
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.Not
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ForwardStateTrackerImplTest : KoinLoggingDisabler() {

    private companion object {
        const val itemKey = "itemKey"
        const val otherItemKey = "otherItemKey"
    }

    private val test get() = Given { ForwardStateTrackerImpl() }

    @Test
    fun `initial item state of any item is empty`() =
            test when_ {
                this[itemKey]
            } then {
                expect(it.result is_ Equal to_ ForwardState())
            }

    @Test
    fun `set the item state can be retrieved again`() {
        val newState = ForwardState(pending = setOf(Id("pending")))
        test when_ {
            this[itemKey] = newState
        } then {
            expect(this[itemKey] is_ Equal to_ newState)
        }
    }

    @Test
    fun `clearFinishedTransmissionFor removes state without pending transmission`() =
            test while_ {
                this[itemKey] = ForwardState(successful = setOf(Id("success")))
            } when_ {
                clearFinishedTransmissionFor(itemKey)
            } then {
                expect(this[itemKey] is_ Equal to_ ForwardState())
            }

    @Test
    fun `clearFinishedTransmissionFor ignores states without other item ids`() =
            test while_ {
                this[itemKey] = ForwardState(successful = setOf(Id("success")))
                this[otherItemKey] = ForwardState(pending = setOf(Id("success")))
            } when_ {
                clearFinishedTransmissionFor(itemKey)
            } then {
                expect(this[otherItemKey] is_ Not - Equal to_ ForwardState())
            }

    @Test
    fun `clearFinishedTransmissionFor does nothing if the item has pending transmissions`() =
            test while_ {
                this[itemKey] = ForwardState(pending = setOf(Id("pending")))
            } when_ {
                clearFinishedTransmissionFor(itemKey)
            } then {
                expect(this[itemKey] is_ Not - Equal to_ ForwardState())
            }
}