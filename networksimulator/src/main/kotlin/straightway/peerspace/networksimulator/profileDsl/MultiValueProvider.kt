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

class MultiValueProvider<T>(
        val name: String,
        private var getter: MultiValueProvider<T>.() -> Iterable<T> = { throw Panic("No value specified for $name") }
) {
    val values get() = getter()
    fun values(vararg items: T): List<T> = items.toList()
    operator fun List<T>.plus(other: T) = add(other)
    operator fun T.unaryPlus(): List<T> = listOf(this)
    operator fun invoke(getter: MultiValueProvider<T>.() -> Iterable<T>) { this.getter = getter }
}

private fun <T> List<T>.add(item: T): List<T> = this + item