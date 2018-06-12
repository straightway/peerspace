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

import straightway.random.Chooser
import straightway.utils.TimeProvider

/**
 * Collection of components used for a peer.
 */
@Suppress("ComplexInterface")
interface Infrastructure {
    val peerDirectory: PeerDirectory
    val network: Network
    val configuration: Configuration
    val knownPeerQueryChooser: Chooser
    val knownPeerAnswerChooser: Chooser
    val forwardStrategy: ForwardStrategy
    val timeProvider: TimeProvider
    val dataQueryHandler: DataQueryHandler
    val dataPushForwarder: DataPushForwarder
    val knownPeersProvider: KnownPeersProvider
}
