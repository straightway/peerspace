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

import straightway.peerspace.crypto.CryptoIdentity
import straightway.peerspace.crypto.Encryptor
import straightway.peerspace.crypto.SignatureChecker
import straightway.peerspace.crypto.Signer

/**
 * Crypto entities for chunkers.
 */
class ChunkerCrypto private constructor(
        val signMode: DataChunkSignMode = DataChunkSignMode.NoKey,
        val signer: Signer? = null,
        val signatureChecker: SignatureChecker? = null,
        val encryptor: Encryptor
) {
    companion object {
        fun forPlainChunk(encryptor: Encryptor) =
                ChunkerCrypto(encryptor = encryptor)
        fun forSignedChunkWithEmbeddedPublicKey(signId: CryptoIdentity, encryptor: Encryptor) =
                ChunkerCrypto(DataChunkSignMode.EmbeddedKey, signId, signId, encryptor)
        fun forSignedChunk(signer: Signer, encryptor: Encryptor) =
                ChunkerCrypto(DataChunkSignMode.NoKey, signer, null, encryptor)
        fun forList(listId: CryptoIdentity, encryptor: Encryptor) =
                ChunkerCrypto(DataChunkSignMode.ListIdKey, listId, listId, encryptor)
    }
}