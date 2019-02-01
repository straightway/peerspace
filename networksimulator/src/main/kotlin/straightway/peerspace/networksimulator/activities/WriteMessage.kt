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
package straightway.peerspace.networksimulator.activities

/*import straightway.peerspace.data.DataChunk
import straightway.peerspace.data.Id
import straightway.peerspace.data.Key
import straightway.peerspace.data.toTimestamp
import straightway.peerspace.net.chunkSize*/
import straightway.peerspace.networksimulator.profile.dsl.Activity
/*import straightway.peerspace.networksimulator.user.ActivityEnvironment
import straightway.peerspace.networksimulator.user.User
import straightway.units.AmountOfData
import straightway.units.UnitValue
import straightway.units.div
import straightway.utils.serializeToByteArray*/

val doWriteMessage = Activity("doWriteMessage") {
    /*val recipient = user.knownUsers.firstOrNull()
    if (recipient != null) {
        writeMessageTo(recipient)
    }*/
}
/*
private fun ActivityEnvironment.writeMessageTo(recipient: User) {
    val size = usage.dataVolume.value
    if (size <= chunkSize) {
        sendOnListOf(recipient, byteArrayOf())
    } else {
        val subChunkKeys = getSubChunksForMessageTo(recipient, size)
        sendOnListOf(recipient, subChunkKeys.serializeToByteArray())
        for (key in subChunkKeys)
            device.peerClient.store(DataChunk(key, byteArrayOf()))
    }
}

private fun ActivityEnvironment.sendOnListOf(recipient: User, data: ByteArray) {
    device.peerClient.store(DataChunk(Key(recipient.id, currentTimestamp, 0), data))
}

private val ActivityEnvironment.currentTimestamp: Long
    get() = timeProvider.now.toTimestamp()

private fun getSubChunksForMessageTo(recipient: User, size: UnitValue<AmountOfData>): List<Key> {
    val numberOfChunks = (size / chunkSize).baseValue.toInt() + 1
    return (1..numberOfChunks).map { Key(Id(recipient.id.identifier + it)) }
}*/