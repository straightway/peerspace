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
@file:Suppress("MagicNumber")
package straightway.peerspace.networksimulator.profile

import straightway.peerspace.networksimulator.profile.dsl.UsageProfile
import straightway.peerspace.networksimulator.activities.doReadSocialMediaFeeds
import straightway.peerspace.networksimulator.activities.doPostOnSocialMediaFeed
import straightway.peerspace.networksimulator.activities.doReadMessages
import straightway.peerspace.networksimulator.activities.doWriteMessage
import straightway.units.byte
import straightway.units.get
import straightway.units.ki
import straightway.units.minute

val readSocialMediaFeeds get() = UsageProfile {
    activity { doReadSocialMediaFeeds }
    duration { 1[minute] }
}

val postOnSocialMediaFeed get() = UsageProfile {
    activity { doPostOnSocialMediaFeed }
    duration { 3[minute] }
    dataVolume { 5[ki(byte)] }
}

val readMessages get() = UsageProfile {
    activity { doReadMessages }
    duration { 1[minute] }
}

val writeMessages get() = UsageProfile {
    activity { doWriteMessage }
    duration { 10[minute] }
    dataVolume { 5[ki(byte)] }
}
