/****************************************************************************
Copyright 2016 github.com/straightway

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 ****************************************************************************/
package straightway.test

import org.junit.jupiter.api.Assertions.assertTrue

interface BaseCondition {
    val result: Boolean
}

interface Condition<TSelector> : BaseCondition {
    fun withExpectation(value: Any) : Condition<TSelector>
}

open class Relation<TSelector>(private val conditionFactory: (Any) -> Condition<TSelector>) {
    fun createConditionFor(testedValue: Any): Condition<TSelector> = conditionFactory(testedValue)
}

fun expect(condition: BaseCondition) = assertTrue(condition.result)

infix fun <TSelector> Any._is(r: Relation<TSelector>) = r.createConditionFor(this)

object to
infix fun Condition<to>.to(value: Any) = this.withExpectation(value)

object _as
infix fun Condition<_as>._as(value: Any) = this.withExpectation(value)
