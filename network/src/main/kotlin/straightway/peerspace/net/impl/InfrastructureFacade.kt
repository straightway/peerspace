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
import straightway.peerspace.net.InfrastructureProvider
import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.toDuration

val InfrastructureProvider.dataChunkStore get() = infrastructure.dataChunkStore
val InfrastructureProvider.peerDirectory get() = infrastructure.peerDirectory
val InfrastructureProvider.network get() = infrastructure.network
val InfrastructureProvider.configuration get() = infrastructure.configuration
val InfrastructureProvider.knownPeerQueryChooser get() = infrastructure.knownPeerQueryChooser
val InfrastructureProvider.knownPeerAnswerChooser get() = infrastructure.knownPeerAnswerChooser
val InfrastructureProvider.forwardStrategy get() = infrastructure.forwardStrategy
val InfrastructureProvider.timeProvider get() = infrastructure.timeProvider
val InfrastructureProvider.dataQueryHandler get() = infrastructure.dataQueryHandler

fun InfrastructureProvider.getPushTargetFor(id: Id) = network.getPushTarget(id)
fun InfrastructureProvider.getQuerySourceFor(id: Id) = network.getQuerySource(id)
fun InfrastructureProvider.nowPlus(duration: UnitNumber<Time>) =
        (timeProvider.currentTime + duration.toDuration())!!
