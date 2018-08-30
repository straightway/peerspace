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
import straightway.peerspace.data.Identifyable
import straightway.peerspace.data.Transmittable
import straightway.utils.isGeneric
import straightway.utils.RequestTypeSelector
import straightway.utils.getHandlers
import straightway.utils.isClass

/**
 * A request which can be automatically handled by a handler function being
 * tagged with RequestHandler and takes Request<T> as parameter.
 */
class Request<T : Transmittable>(
        val typeSelector: RequestTypeSelector,
        val remotePeerId: Id,
        val content: T
) : Identifyable {

    override val id get() = content.id

    override fun toString() = "Request(${remotePeerId.identifier} -> $content)"

    override fun equals(other: Any?) =
            other is Request<*> &&
            remotePeerId == other.remotePeerId &&
            content == other.content

    override fun hashCode() = remotePeerId.hashCode() xor content.hashCode()

    companion object {
        inline operator fun <reified T : Transmittable> invoke(originatorId: Id, content: T) =
                Request(isGeneric(Request::class, isClass(T::class)),
                        originatorId,
                        content)
        fun createDynamically(originatorId: Id, content: Transmittable): Request<*> =
                Request(isGeneric(Request::class, isClass(content::class)),
                        originatorId,
                        content)
    }
}

@Suppress("UNUSED_PARAMETER")
infix fun Any.handle(request: Request<*>) =
    getHandlers<RequestHandler>(request.typeSelector).forEach { it(request) }
