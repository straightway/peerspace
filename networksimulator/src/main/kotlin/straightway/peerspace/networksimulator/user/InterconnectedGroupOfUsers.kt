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
package straightway.peerspace.networksimulator.user

import straightway.random.Chooser

/**
 * Collection of users which are loosely interconnected to each other, i.e. ther
 * is a path via the knownUsers bean from any member to any other member.
 */
class InterconnectedGroupOfUsers(private val chooser: Chooser, val members: List<User>) {

    /**
     * Get a InterconnectedGroupOfUsers which consists of members of both input
     * groups. Also makes sure the result is also interconnected.
     */
    fun unifyWith(other: InterconnectedGroupOfUsers) =
            InterconnectedGroupOfUsers(chooser, members + other.members).also {
                if (members.any() && other.members.any()) {
                    val myPeer = chooser.chooseFrom(members, 1).single()
                    val otherPeer = chooser.chooseFrom(other.members, 1).single()
                    myPeer.knownUsers.add(otherPeer)
                    otherPeer.knownUsers.add(myPeer)
                }
            }
}