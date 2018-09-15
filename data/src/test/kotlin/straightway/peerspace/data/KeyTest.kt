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
import straightway.expr.minus
import straightway.testing.flow.Equal
import straightway.testing.flow.False
import straightway.testing.flow.Not
import straightway.testing.flow.Null
import straightway.testing.flow.True
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import straightway.utils.serializeToByteArray
import straightway.utils.deserializeTo

class KeyTest {

    private companion object {
        val id = Id("ID")
        val timestamp = 83L
        val fullKey = Key(id, timestamp, epoch = 2)
    }

    @Test
    fun `id passed to two argument constructor is returned by according getter`() =
            expect(Key(id, timestamp).id is_ Equal to_ id)

    @Test
    fun `timestamp passed to two argument constructor is returned by according getter`() =
            expect(Key(id, timestamp).timestamp is_ Equal to_ timestamp)

    @Test
    fun `id passed to one argument constructor is returned by according getter`() =
            expect(Key(id).id is_ Equal to_ id)

    @Test
    fun `timestamp of object created by one argument constructor is 0`() =
            expect(Key(id).timestamp is_ Equal to_ 0L)

    @Test
    fun `if not specified at construction, epoch is null`() =
            expect(Key(id, timestamp).epoch is_ Null)

    @Test
    fun `id constructor called with timestamp zero and non-null epoch, epoch is null anyway`() =
            expect(Key(id, 0L, 2).epoch is_ Null)

    @Test
    fun `copy untimed key with non-null epoch yields exact copy with null epoch`() =
            expect(Key(id).copy(epoch = 2) is_ Equal to_ Key(id))

    @Test
    fun `perfect copies are equal`() =
            expect(fullKey is_ Equal to_ fullKey.copy())

    @Test
    fun `keys differing by id differ`() =
            expect(fullKey is_ Not - Equal to_ fullKey.copy(id = Id("otherId")))

    @Test
    fun `keys differing by timestamp differ`() =
            expect(fullKey is_ Not - Equal to_ fullKey.copy(timestamp = 3))

    @Test
    fun `keys differing by epoch differ`() =
            expect(fullKey is_ Not - Equal to_ fullKey.copy(epoch = null))

    @Test
    fun `perfect copies have equal hash codes`() =
            expect(fullKey.hashCode() is_ Equal to_ fullKey.copy().hashCode())

    @Test
    fun `keys differing by id have different hash code`() =
            expect(fullKey.hashCode() is_
                           Not - Equal to_ fullKey.copy(id = Id("otherId")).hashCode())

    @Test
    fun `keys differing by timestamp have different hash code`() =
            expect(fullKey.hashCode() is_
                           Not - Equal to_ fullKey.copy(timestamp = 3).hashCode())

    @Test
    fun `keys differing by epoch have different hash code`() =
            expect(fullKey.hashCode() is_
                           Not - Equal to_ fullKey.copy(epoch = null).hashCode())

    @Test
    fun `Key is serializable`() {
        val sut = Key(id, timestamp)
        val serialized = sut.serializeToByteArray()
        val deserialized = serialized.deserializeTo<Key>()
        expect(deserialized is_ Equal to_ sut)
    }

    @Test
    fun `has serialVersionUID`() =
            expect(Key.serialVersionUID is_ Equal to_ 1L)

    @Test
    fun `timestamps is a single element range`() =
            expect(Key(id, timestamp).timestamps is_ Equal to_ LongRange(timestamp, timestamp))

    @Test
    fun `isUntimed returns true if timestamp is 0`() =
            expect(Key(id, 0).isUntimed is_ True)

    @Test
    fun `isUntimed returns false if timestamp is not 0`() =
            expect(Key(id, 1).isUntimed is_ False)

    @Test
    fun `toString for untimed key returns just the id`() =
            expect(Key(id).toString() is_ Equal to_ "Key(${id.identifier})")

    @Test
    fun `toString for timed key returns the id and timestamp`() =
            expect(Key(id, timestamp).toString() is_
                           Equal to_ "Key(${id.identifier}@$timestamp)")

    @Test
    fun `toString for timed key with epoch returns the id, timestamp and epoch`() =
            expect(Key(id, timestamp, 2).toString() is_
                           Equal to_ "Key(${id.identifier}@$timestamp[2])")
}