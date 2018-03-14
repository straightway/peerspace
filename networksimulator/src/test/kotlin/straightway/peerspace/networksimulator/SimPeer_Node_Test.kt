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

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.sim.net.TransmissionStream
import straightway.testing.bdd.Given
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.is_

class SimPeer_Node_Test {

    private val test get() = Given {
        object {
            val upload = mock<TransmissionStream>()
            val download = mock<TransmissionStream>()
            val sut = SimPeer("id", upload, download, mutableMapOf())
        }
    }

    @Test
    fun `upload stream is as specified`() =
            test when_ { sut.uploadStream } then { it.result is_ Same as_ upload }

    @Test
    fun `download stream is as specified`() =
            test when_ { sut.downloadStream } then { it.result is_ Same as_ download }
}