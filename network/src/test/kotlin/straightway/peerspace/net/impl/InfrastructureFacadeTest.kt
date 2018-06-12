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
/*
import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.net.Infrastructure
import straightway.peerspace.net.InfrastructureProvider
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.units.get
import straightway.units.second
import straightway.utils.TimeProvider
import java.time.LocalDateTime

class InfrastructureFacadeTest {

    private companion object {
        val currentTime = LocalDateTime.of(2000, 1, 1, 0, 0)
    }

    private data class Provider(override val infrastructure: Infrastructure)
        : InfrastructureProvider

    private val test get() =
        Given {
            object {
                val infrastructure = Provider(createInfrastructure(
                        timeProvider = mock<TimeProvider> {
                            on { currentTime }.thenReturn(currentTime)
                        }
                ))
            }
        }

    @Test
    fun `nowPlus yields current time for zero duration`() =
            test when_ { infrastructure.nowPlus(0[second]) } then {
                expect(it.result is_ Equal to_ currentTime)
            }

    @Test
    fun `nowPlus yields future time for positive duration`() =
            test when_ { infrastructure.nowPlus(1[second]) } then {
                expect(it.result is_ Equal to_ currentTime.plusSeconds(1))
            }

    @Test
    fun `nowPlus yields past time for negative duration`() =
            test when_ { infrastructure.nowPlus(-1[second]) } then {
                expect(it.result is_ Equal to_ currentTime.minusSeconds(1))
            }
}*/