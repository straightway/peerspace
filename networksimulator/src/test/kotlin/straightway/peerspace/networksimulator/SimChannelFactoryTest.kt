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

package straightway.peerspace.networksimulator/*
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.testing.TestBase
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class SimChannelFactoryTest : TestBase<SimChannelFactory>() {

    @BeforeEach
    fun setup() {
        sut = SimChannelFactory()
    }

    @Test
    fun createsSimChannelInstances() {
        val result = sut.create("id")
        expect(result::class is_ equal to_ SimChannel::class)
    }

    @Test
    fun createdInstanceHasPassedId() {
        val result = sut.create("id")
        expect(result.id is_ equal to_ "id")
    }

    @Test
    fun creatingTheSameChannelTwiceYieldsSameInstance() {
        val result1 = sut.create("id")
        val result2 = sut.create("id")
        expect(result1 is_ Same as_ result2)
    }
}