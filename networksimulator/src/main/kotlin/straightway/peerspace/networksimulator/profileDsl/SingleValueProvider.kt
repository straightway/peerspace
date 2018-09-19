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
package straightway.peerspace.networksimulator.profileDsl

import straightway.error.Panic

class SingleValueProvider<T>(
        val name: String,
        private var getter: SingleValueProvider<T>.() -> T = { throw Panic("No value specified for $name") }
) {
    val value get() = getter()
    operator fun invoke(getter: SingleValueProvider<T>.() -> T) { this.getter = getter }
}
