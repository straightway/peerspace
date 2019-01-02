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

import straightway.peerspace.networksimulator.profile.dsl.DeviceUsageProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.units.get
import straightway.units.hour
import straightway.units.minute
import straightway.units.second

val homeUsedPc = DeviceUsageProfile {
    onlineTimes {
        +Weekly.workdays { 19.0[hour]..22.0[hour] }
        +Weekly.weekends { 19.0[hour]..24.0[hour] }
        +Weekly.sundays { 0.0[hour]..2.0[hour] }
    }
    usages {
        +readSocialMediaFeeds {
            time { Weekly.workdays { 19.0[hour]..22.0[hour] } }
            duration { 30.0[second] }
            numberOfTimes { 10 }
        }
        +postOnSocialMediaFeed {
            time { Weekly.workdays { 19.0[hour]..22.0[hour] } }
            duration { 2.0[minute] }
            numberOfTimes { 3 }
        }
    }
    device { pc }
}

val mobilePhone = DeviceUsageProfile {
    onlineTimes { +Weekly.eachDay { 0.0[hour]..24.0[hour] } }
    usages {
        +postOnSocialMediaFeed {
            time { Weekly.eachDay { 8.0[hour]..22.0[hour] } }
            numberOfTimes { 4 }
            duration { 5.0[minute] }
        }
        +readSocialMediaFeeds {
            time { Weekly.eachDay { 8.0[hour]..22.0[hour] } }
            numberOfTimes { 20 }
            duration { 1.0[minute] }
        }
        +readMessages {
            time { Weekly.eachDay { 8.0[hour]..22.0[hour] } }
            duration { 1.0[minute] }
            numberOfTimes { 50 }
        }
        +writeMessages {
            time { Weekly.eachDay { 8.0[hour]..22.0[hour] } }
            numberOfTimes { 15 }
            duration { 1.0[minute] }
        }
    }
    device { mobileDevice }
}

val workPc = DeviceUsageProfile {
    onlineTimes {
        +Weekly.workdays { 8.0[hour]..17.0[hour] }
    }
    usages {
        +readMessages {
            time { onlineTimes.values.single() }
            duration { 2.0[minute] }
            numberOfTimes { 40 }
        }
        +writeMessages {
            time { onlineTimes.values.single() }
            duration { 10.0[minute] }
            numberOfTimes { 15 }
        }
    }
    device { pc }
}