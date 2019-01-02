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
package straightway.peerspace.networksimulator.profile

import straightway.peerspace.networksimulator.profile.dsl.UserProfile
import straightway.peerspace.networksimulator.profile.dsl.Weekly
import straightway.units.get
import straightway.units.hour

@Suppress("MagicNumber")
val defaultActivityTimes = listOf(
        Weekly.mondays { +7.0[hour]..23.0[hour] },
        Weekly.tuesdays { +7.0[hour]..23.0[hour] },
        Weekly.wednesdays { +7.0[hour]..23.0[hour] },
        Weekly.thursdays { +7.0[hour]..22.0[hour] },
        Weekly.fridays { +7.0[hour]..24.0[hour] },
        Weekly.saturdays { +0.0[hour]..1.0[hour]; +9.0[hour]..24.0[hour] },
        Weekly.sundays { +0.0[hour]..2.0[hour]; +10.0[hour]..23.0[hour] }
)

val officeWorker = UserProfile {
    usedDevices {
        +workPc
        +homeUsedPc
        +mobilePhone
    }
    activityTimes { +defaultActivityTimes }
}

val nonOfficeWorker = UserProfile {
    usedDevices {
        +homeUsedPc
        +mobilePhone
    }
    activityTimes { +defaultActivityTimes }
}

val mobileOnlyUser = UserProfile {
    usedDevices { +mobilePhone }
    activityTimes { +defaultActivityTimes }
}

val homePcOnlyUser = UserProfile {
    usedDevices { +homeUsedPc }
    activityTimes { +defaultActivityTimes }
}