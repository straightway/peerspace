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
package straightway.peerspace.net.impl

import straightway.peerspace.data.Id
import straightway.koinutils.KoinModuleComponent
import straightway.peerspace.net.Network
import straightway.peerspace.net.PushTarget
import straightway.peerspace.net.QuerySource

/**
 * Productive implementation of the Network interface.
 */
class NetworkImpl : Network, KoinModuleComponent by KoinModuleComponent() {
    override fun getPushTarget(id: Id) =
            context.get<PushTarget> { mapOf("id" to id) }
    override fun getQuerySource(id: Id) =
            context.get<QuerySource> { mapOf("id" to id) }
}