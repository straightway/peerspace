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

import straightway.peerspace.transport.ChunkProperties
import straightway.peerspace.transport.ChunkTreeInfo
import java.util.*

fun ChunkProperties.getChunkTreeInfoFor(dataSizeBytes: Int): ChunkTreeInfo {
       var cached = chunkTreePropertyCache[this] ?: listOf(ChunkTreeProperty(0, this))
       var candidate = cached.firstOrNull { dataSizeBytes <= it.maxSizeBytes } ?: cached.last()
       while (candidate.maxSizeBytes < dataSizeBytes) {
           candidate = ChunkTreeProperty(candidate.maxSizeBytes, this)
           cached = cached + candidate
           chunkTreePropertyCache[this] = cached
       }

       return ChunkTreeInfo(
               candidate.maxSubTreeSizeBytes,
               candidate.getDirectoryPayloadSizeBytes(dataSizeBytes))
   }

private val chunkTreePropertyCache =
        Collections.synchronizedMap(mutableMapOf<ChunkProperties, List<ChunkTreeProperty>>())

private class ChunkTreeProperty(
       val maxSubTreeSizeBytes: Int,
       private val chunkProperties: ChunkProperties
) {
   val maxSizeBytes: Int by lazy {
       if (maxSubTreeSizeBytes <= 0) chunkProperties.version0PayloadSizeInCryptoContainerBytes
       else getSizeBytesForReferences(chunkProperties.maxReferences)
   }

   fun getDirectoryPayloadSizeBytes(dataSizeBytes: Int): Int =
           chunkProperties.getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes(
                   getNumberOfReferences(dataSizeBytes))

   private fun getNumberOfReferences(dataSizeBytes: Int) =
           (0..chunkProperties.maxReferences).first {
               dataSizeBytes <= getSizeBytesForReferences(it)
           }

   private fun getSizeBytesForReferences(references: Int): Int =
           references * maxSubTreeSizeBytes +
           chunkProperties.getMaxVersion2PayloadSizeWithReferencesInCryptoContainerBytes(references)
}