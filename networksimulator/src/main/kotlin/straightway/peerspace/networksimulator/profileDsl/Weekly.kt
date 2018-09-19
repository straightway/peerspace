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

import straightway.units.Time
import straightway.units.UnitNumber
import straightway.units.UnitValue

class Weekly {
    companion object {
        val monday get() = Weekly()
        val tuesday get() = Weekly()
        val wednesday get() = Weekly()
        val thursday get() = Weekly()
        val friday get() = Weekly()
        val saturday get() = Weekly()
        val sunday get() = Weekly()
        val workday get() = Weekly()
        val weekend get() = Weekly()
        val eachDay get() = Weekly()
    }
    var time = SingleValueProvider<ClosedRange<UnitNumber<Time>>>("time")

    fun values(vararg items: ClosedRange<UnitNumber<Time>>) = items.toList()

    operator fun <T: Number> List<ClosedRange<UnitNumber<Time>>>.plus(other: ClosedRange<UnitValue<T, Time>>): List<ClosedRange<UnitNumber<Time>>> =
            add(other)

    operator fun <T: Number> ClosedRange<UnitValue<T, Time>>.unaryPlus(): List<ClosedRange<UnitNumber<Time>>> = listOf<ClosedRange<UnitNumber<Time>>>(start..endInclusive)

    operator fun invoke(valueGetter: Weekly.() -> ClosedRange<UnitNumber<Time>>): Weekly {
        time {
            @Suppress("UNUSED_EXPRESSION")
            valueGetter()
        }
        return this
    }
}

private fun <T: Number> List<ClosedRange<UnitNumber<Time>>>.add(i: ClosedRange<UnitValue<T, Time>>): List<ClosedRange<UnitNumber<Time>>> = this + (i.start as UnitNumber<Time>..i.endInclusive)