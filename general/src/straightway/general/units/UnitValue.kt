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
package straightway.general.units

import straightway.general.numbers.compareTo
import straightway.general.numbers.div
import straightway.general.numbers.times

data class UnitValue<TValue: Number, TQuantity: Quantity>(
    val value: TValue,
    val unit: TQuantity) : Comparable<UnitValue<TValue, TQuantity>>
{
    val scaledValue by lazy { value * unit.scale.magnitude / unit.siScaleCorrection.magnitude }

    override fun toString() =
        "$value $unit".trimEnd()
    override fun equals(other: Any?) =
        other is UnitValue<*, *> &&
        other.unit::class == unit::class &&
        other.scaledValue == scaledValue
    override fun compareTo(other: UnitValue<TValue, TQuantity>) =
        scaledValue.compareTo(other.scaledValue)
}

operator fun <TNum: Number, TQuantity: Quantity> TNum.get(unit: TQuantity) =
    UnitValue(this, unit)
