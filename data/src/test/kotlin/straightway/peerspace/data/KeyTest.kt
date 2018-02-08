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

package straightway.peerspace.data

import org.junit.jupiter.api.Test
import straightway.testing.flow.equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class KeyTest {
    @Test
    fun `id passed to two argument constructor is returned by according getter`() =
            expect(Key("ID", 83).id is_ equal to_ "ID")

    @Test
    fun `timestamp passed to two argument constructor is returned by according getter`() =
            expect(Key("ID", 83).timestamp is_ equal to_ 83L)

    @Test
    fun `id passed to one argument constructor is returned by according getter`() =
            expect(Key("ID").id is_ equal to_ "ID")

    @Test
    fun `timestamp of object created by one argument constructor is 0`() =
            expect(Key("ID").timestamp is_ equal to_ 0L)

    @Test
    fun `Key is serializable`() {
        val sut = Key("1234", 83)
        val serialized = sut.serializeToByteArray()
        val deserialized = serialized.deserializeTo<Key>()
        expect(deserialized is_ equal to_ sut)
    }
}