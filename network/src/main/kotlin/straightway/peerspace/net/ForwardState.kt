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

package straightway.peerspace.net

import straightway.peerspace.data.Id

/**
 * State of forwarding a query or chunk to to other peers.
 */
data class ForwardState(
        val successful: Set<Id> = setOf(),
        val failed: Set<Id> = setOf(),
        val pending: Set<Id> = setOf()
) {
    fun setPending(id: Id) = if (id in pending) this else copy(
            successful = successful - id,
            pending = pending + id,
            failed = failed - id)

    fun setSuccess(id: Id) = if (id in successful) this else copy(
            successful = successful + id,
            pending = pending - id,
            failed = failed - id)

    fun setFailed(id: Id) = if (id in failed) this else copy(
            successful = successful - id,
            pending = pending - id,
            failed = failed + id)
}