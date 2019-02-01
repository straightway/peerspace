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
package straightway.peerspace.transport

import com.nhaarman.mockito_kotlin.mock
import org.junit.jupiter.api.Test
import straightway.peerspace.crypto.SignatureChecker
import straightway.peerspace.data.Id
import straightway.peerspace.transport.impl.createList
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class TransportTest {

    @Test
    fun `createList - generates id from signatureCheckKey`() {
        val keyBytes = byteArrayOf(1, 2, 3)
        val signatureChecker = mock<SignatureChecker> {
            on { signatureCheckKey }.thenAnswer { keyBytes }
        }
        expect(createList(signatureChecker) is_ Equal to_ Id(keyBytes))
    }
}