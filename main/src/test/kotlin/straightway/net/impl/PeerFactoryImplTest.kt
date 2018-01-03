/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.net

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.net.impl.PeerFactoryImpl
import straightway.net.impl.PeerNetworkStub
import straightway.testing.TestBase
import straightway.testing.flow.*

class PeerFactoryImplTest : TestBase<PeerFactoryImpl>() {

    @BeforeEach
    fun setup() {
        sut = PeerFactoryImpl()
    }

    @Test
    fun createsPeerInstances() {
        val result = sut.create("id")
        expect(result::class _is same _as PeerNetworkStub::class)
    }

    @Test
    fun createdPeerHasProperId() {
        val result = sut.create("id")
        expect(result.id _is equal _to "id")
    }
}