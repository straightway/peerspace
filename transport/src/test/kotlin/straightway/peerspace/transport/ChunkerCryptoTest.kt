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
import straightway.peerspace.crypto.CryptoIdentity
import straightway.peerspace.crypto.Encryptor
import straightway.peerspace.crypto.Signer
import straightway.peerspace.transport.impl.DataChunkSignMode
import straightway.testing.flow.Equal
import straightway.testing.flow.Null
import straightway.testing.flow.Same
import straightway.testing.flow.as_
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_

class ChunkerCryptoTest {

    @Test
    fun `forPlainChunk only has encryptor`() {
        val encryptor = mock<Encryptor>()
        val sut = ChunkerCrypto.forPlainChunk(encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.NoKey)
        expect(sut.signer is_ Null)
        expect(sut.signatureChecker is_ Null)
    }

    @Test
    fun `forList has list indentity as signer and checker and encryptor`() {
        val encryptor = mock<Encryptor>()
        val listId = mock<CryptoIdentity>()
        val sut = ChunkerCrypto.forList(listId, encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.ListIdKey)
        expect(sut.signer is_ Same as_ listId)
        expect(sut.signatureChecker is_ Same as_ listId)
    }

    @Test
    fun `forSignedChunk has signer and encryptor but no checker`() {
        val encryptor = mock<Encryptor>()
        val signer = mock<Signer>()
        val sut = ChunkerCrypto.forSignedChunk(signer, encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.NoKey)
        expect(sut.signer is_ Same as_ signer)
        expect(sut.signatureChecker is_ Null)
    }

    @Test
    fun `forSignedChunkWithEmbeddedPublicKey has signer and encryptor but no checker`() {
        val encryptor = mock<Encryptor>()
        val cryptoId = mock<CryptoIdentity>()
        val sut = ChunkerCrypto.forSignedChunkWithEmbeddedPublicKey(cryptoId, encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.EmbeddedKey)
        expect(sut.signer is_ Same as_ cryptoId)
        expect(sut.signatureChecker is_ Same as_ cryptoId)
    }
}