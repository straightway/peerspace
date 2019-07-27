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
import straightway.expr.minus
import straightway.peerspace.crypto.CryptoIdentity
import straightway.peerspace.crypto.Encryptor
import straightway.peerspace.crypto.Signer
import straightway.testing.flow.*

class ChunkerCryptoTest {

    @Test
    fun `forPlainChunk only has encryptor`() {
        val sut = ChunkerCrypto.forPlainChunk(encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.NoKey)
        expect(sut.signer is_ Null)
        expect(sut.signatureChecker is_ Null)
    }

    @Test
    fun `forList has list indentity as signer and checker and encryptor`() {
        val sut = ChunkerCrypto.forList(listId, encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.ListIdKey)
        expect(sut.signer is_ Same as_ listId)
        expect(sut.signatureChecker is_ Same as_ listId)
    }

    @Test
    fun `forSignedChunk has signer and encryptor but no checker`() {
        val sut = ChunkerCrypto.forSignedChunk(signer, encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.NoKey)
        expect(sut.signer is_ Same as_ signer)
        expect(sut.signatureChecker is_ Null)
    }

    @Test
    fun `forSignedChunkWithEmbeddedPublicKey has signer and encryptor but no checker`() {
        val sut = ChunkerCrypto.forSignedChunkWithEmbeddedPublicKey(cryptoId, encryptor)
        expect(sut.encryptor is_ Same as_ encryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.EmbeddedKey)
        expect(sut.signer is_ Same as_ cryptoId)
        expect(sut.signatureChecker is_ Same as_ cryptoId)
    }

    @Test
    fun `withContentCryptor creates copy`() {
        val original = ChunkerCrypto.forList(listId, encryptor)
        val sut = original.withContentCryptor(newEcryptor)
        expect(sut is_ Not - Same as_ original)
    }

    @Test
    fun `withContentCryptor returns copy with same sign mode`() {
        val sut = ChunkerCrypto.forList(listId, encryptor).withContentCryptor(newEcryptor)
        expect(sut.signMode is_ Equal to_ DataChunkSignMode.ListIdKey)
    }

    @Test
    fun `withContentCryptor returns copy with same signer`() {
        val sut = ChunkerCrypto.forSignedChunk(signer, encryptor).withContentCryptor(newEcryptor)
        expect(sut.signer is_ Same as_ signer)
    }

    @Test
    fun `withContentCryptor returns copy with same signatureChecker`() {
        val sut = ChunkerCrypto.forSignedChunkWithEmbeddedPublicKey(cryptoId, encryptor).withContentCryptor(newEcryptor)
        expect(sut.signatureChecker is_ Same as_ cryptoId)
    }

    @Test
    fun `withContentCryptor returns copy with given encryptor`() {
        val sut = ChunkerCrypto.forList(listId, encryptor).withContentCryptor(newEcryptor)
        expect(sut.encryptor is_ Same as_ newEcryptor)
    }

    // region Private

    private val encryptor = mock<Encryptor>()
    private val newEcryptor = mock<Encryptor>()
    private val signer = mock<Signer>()
    private val cryptoId = mock<CryptoIdentity>()
    private val listId = mock<CryptoIdentity>()

    // endregion
}