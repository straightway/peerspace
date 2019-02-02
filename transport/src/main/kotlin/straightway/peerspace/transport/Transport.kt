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

import straightway.peerspace.crypto.SignatureChecker
import straightway.peerspace.data.Id

/**
 * Unchunked access to the Peerspace network.
 */
@Suppress("LongParameterList")
interface Transport {
    fun store(data: ByteArray, crypto: ChunkerCrypto): Id
    fun post(listId: Id, data: ByteArray, crypto: ChunkerCrypto)
    fun query(id: Id, crypto: DeChunkerCrypto, querySetup: DataQueryCallback.() -> Unit)
    fun query(query: ListQuery, crypto: DeChunkerCrypto, querySetup: ListQueryCallback.() -> Unit)
}


fun createList(signatureChecker: SignatureChecker) = Id(signatureChecker.signatureCheckKey)
