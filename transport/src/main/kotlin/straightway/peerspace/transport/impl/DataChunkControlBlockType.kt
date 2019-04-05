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
package straightway.peerspace.transport.impl

/**
 * Enumeration of all control block types which may occur in version 1
 * data chunks.
 */
@Suppress("MagicNumber")
enum class DataChunkControlBlockType(val id: Byte) {
    Signature(0x01),
    PublicKey(0x02),
    ContentKey(0x03),
    ReferencedChunk(0x04)
}