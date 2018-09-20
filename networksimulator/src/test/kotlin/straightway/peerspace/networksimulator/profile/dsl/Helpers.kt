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

import straightway.error.Panic
import straightway.testing.bdd.Given
import straightway.testing.flow.Same
import straightway.testing.flow.Throw
import straightway.testing.flow.as_
import straightway.testing.flow.does
import straightway.testing.flow.expect
import straightway.testing.flow.is_

typealias ProfileCreator<T> = (T.() -> Unit) -> T

fun <T> testProfile(sutCreator: ProfileCreator<T>) = sutCreator

fun <T, V> ProfileCreator<T>.testSingleValue(
        testValue: V,
        valueGetter: T.() -> SingleValueProvider<V>
) {
    Given {
        this {}
    } when_ {
        valueGetter().value
    } then {
        expect({ it.result } does Throw.type<Panic>())
    }

    Given {
        this { valueGetter().invoke { testValue } }
    } when_ {
        valueGetter().value
    } then {
        expect(it.result is_ Same as_ testValue as Any)
    }
}

fun <T, V> ProfileCreator<T>.testMultiValue(valueGetter: T.() -> MultiValueProvider<V>
) {
    Given {
        this {}
    } when_ {
        valueGetter().values
    } then {
        expect({ it.result } does Throw.type<Panic>())
    }

    val testValues = listOf<V>()
    Given {
        this { valueGetter().invoke { testValues } }
    } when_ {
        valueGetter().values
    } then {
        expect(it.result is_ Same as_ testValues)
    }
}