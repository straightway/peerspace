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
import straightway.utils.indent

/**
 * Multiple values under one name.
 */
abstract class MultiValue<T>(name: String) : Named(name) {

    inner class Configurator {
        val values = mutableListOf<T>()
        fun values(vararg vs: T) {
            values.clear()
            values.addAll(vs)
        }
        operator fun T.unaryPlus() = values.add(this)
        operator fun Iterable<T>.unaryPlus() = values.addAll(this)
    }

    val values get() = valuesBackingField ?: throw Panic("No value specified for $name")

    operator fun invoke(getter: Configurator.() -> Unit) = setValuesFrom {
        val configurator = Configurator()
        configurator.getter()
        configurator.values
    }

    override fun toString() = with(valuesBackingField) {
        if (this != null) determineStringRepresentation() else "$name = <unset>"
    }

    protected abstract fun setValuesFrom(getter: () -> List<T>)

    protected abstract val valuesBackingField: Iterable<T>?

    private fun Iterable<T>.determineStringRepresentation() = with(map { it.toString() }) {
        if (any { it.contains('\n') })
            "$name = [\n" +
                    "${map { it.indent(2) }.joinToString(",\n")}\n" +
                    "]"
        else "$name = [${joinToString(", ")}]"
    }
}