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
package straightway.numbers

operator fun <T : Number> T.unaryPlus() = this
@Suppress("UNCHECKED_CAST")
operator fun <T : Number> T.unaryMinus() = (0 - this) as T
operator fun Number.plus(other: Number) : Number = unify(this, other).apply { plus }
operator fun Number.minus(other: Number) : Number = unify(this, other).apply { minus }
operator fun Number.times(other: Number) : Number = unify(this, other).apply { times }
operator fun Number.div(other: Number) : Number = unify(this, other).apply { div }
operator fun Number.rem(other: Number) : Number = unify(this, other).apply { rem }
operator fun Number.compareTo(other: Number) : Int = unify(this, other).apply { compare }

fun round(num: Number) : Number = num.(NumberInfo[num].round)()

fun unify(a: Number, b: Number) : Pair<Number, Number> {
    val aInfo = NumberInfo[a]
    val bInfo = NumberInfo[b]
    return if (aInfo.prio < bInfo.prio) Pair(bInfo.unify(a), b) else Pair(a, aInfo.unify(b))
}

@Suppress("UNCHECKED_CAST")
private fun <T: Number> Pair<Number, Number>.apply(op: NumberInfo.() -> Number.(Number) -> Number) : T {
    val numberInfo = NumberInfo[this.first]
    return this.first.(numberInfo.op())(this.second) as T
}
