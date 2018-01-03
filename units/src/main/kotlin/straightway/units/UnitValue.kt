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
@file:Suppress("UNCHECKED_CAST")

package straightway.units

import straightway.error.Panic
import straightway.numbers.*
import java.io.Serializable

/**
 * Convenience type for unit values with Number.
 */
typealias UnitNumber<TQuantity> = UnitValue<*, TQuantity>

/**
 * Combine a numerical value with a unit.
 * Basic artihmetic is available for unit values, either with other unit values
 * or with scalars.
 */
data class UnitValue<TValue: Number, TQuantity: Quantity>(
    val value: TValue,
    val unit: TQuantity) : Comparable<UnitValue<*, TQuantity>>, Serializable
{
    val baseValue get() = value * unit.siScale.magnitude * unit.baseMagnitude + unit.valueShift

    operator fun get(newUnit: TQuantity) =
        UnitValue((baseValue - newUnit.valueShift) * newUnit.siScale.reciproke.magnitude / newUnit.baseMagnitude, newUnit)

    operator fun unaryMinus() = UnitValue<TValue, TQuantity>(-value, unit)
    operator fun unaryPlus() = this

    override fun toString() =
        "$value $unit".trimEnd()

    override fun equals(other: Any?) =
        other is UnitValue<*, *> &&
            other.unit.id == unit.id &&
            other.baseValue.compareTo(baseValue) == 0

    override fun hashCode() =
        value.hashCode() xor unit.hashCode()

    override fun compareTo(other: UnitValue<*, TQuantity>) =
        baseValue.compareTo(other.baseValue)
}

/**
 * Create a UnitValue by combining a number with a given unit in square brackets.
 */
operator fun <TNum: Number, TQuantity: Quantity> TNum.get(unit: TQuantity) =
    UnitValue(this, unit)

/**
 * Convert the given unit value to a value with another compatibe unit.
 * This can be useful to assign a simplified unit when using arithmetic operators
 * with unit values.
 */
operator fun <TQuantity1 : Quantity, TQuantity2 : Quantity>
    UnitNumber<TQuantity1>.get(unit: TQuantity2) =
    if (this.unit.id != unit.id) throw Panic("Incompatible units: ${unit} for ${this} ")
    else (baseValue - unit.valueShift)[unit.withScale(unit.siScaleCorrection)][unit] as UnitNumber<TQuantity2>

operator fun <TNum1 : Number, TNum2 : Number, TQuantity : Quantity>
    UnitValue<TNum1, TQuantity>.plus(other: UnitValue<TNum2, TQuantity>) =
    (baseValue + other.baseValue)[other.unit.withScale(uni)] as UnitValue<Number, TQuantity>

operator fun <TNum1 : Number, TNum2 : Number, TQuantity : Quantity>
    UnitValue<TNum1, TQuantity>.minus(other: UnitValue<TNum2, TQuantity>) =
    (baseValue - other.baseValue)[other.unit.withScale(uni)] as UnitValue<Number, TQuantity>

operator fun <TNum1 : Number, TNum2 : Number, TQuantity1 : Quantity, TQuantity2 : Quantity>
    UnitValue<TNum1, TQuantity1>.times(other: UnitValue<TNum2, TQuantity2>) =
    (baseValue * other.baseValue)[unit.withScale(uni) * other.unit.withScale(uni)]

operator fun <TNum : Number, TQuantity : Quantity>
    TNum.times(other: UnitValue<TNum, TQuantity>) =
    UnitValue<TNum, TQuantity>((this * other.value) as TNum, other.unit)

operator fun <TNum : Number, TQuantity : Quantity>
    UnitValue<TNum, TQuantity>.times(other: TNum) = other * this

operator fun <TNum1 : Number, TNum2 : Number, TQuantity1 : Quantity, TQuantity2 : Quantity>
    UnitValue<TNum1, TQuantity1>.div(other: UnitValue<TNum2, TQuantity2>) =
    (baseValue / other.baseValue)[unit.withScale(uni) / other.unit.withScale(uni)]

operator fun <TNum : Number, TQuantity : Quantity>
    TNum.div(other: UnitValue<TNum, TQuantity>) =
    UnitValue<TNum, Reciproke<TQuantity>>((this / other.value) as TNum, Reciproke(other.unit))

operator fun <TNum : Number, TQuantity : Quantity>
    UnitValue<TNum, TQuantity>.div(other: TNum) =
    UnitValue<TNum, TQuantity>((value / other) as TNum, unit)

fun <TQuantity : Quantity> min(vararg items: UnitNumber<TQuantity>) = items.min()!!

fun <TQuantity : Quantity> max(vararg items: UnitNumber<TQuantity>) = items.max()!!

fun <TNum : Number, TQuantity : Quantity> abs(value: UnitValue<TNum, TQuantity>) =
    if (value.value < 0) -value else value