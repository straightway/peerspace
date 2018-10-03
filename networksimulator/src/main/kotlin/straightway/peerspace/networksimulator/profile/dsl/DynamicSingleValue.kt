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
package straightway.peerspace.networksimulator.profile.dsl

/**
 * Provide a single named value by a getter function.
 */
class DynamicSingleValue<T>(name: String) : SingleValue<T>(name) {
    override operator fun invoke(getter: SingleValue<T>.() -> T) {
        this.getter = getter
    }
    override val valueBackingField: T? get() = getter()
    private var getter: DynamicSingleValue<T>.() -> T? = { null }
}
