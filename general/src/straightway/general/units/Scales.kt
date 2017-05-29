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
@file:Suppress("UNUSED")

package straightway.general.units

val atto = UnitScale(1e-18)
val femto = UnitScale(1e-15)
val pico = UnitScale(1e-12)
val nano = UnitScale(1e-9)
val micro = UnitScale("µ", 1e-6)
val milli = UnitScale(1e-3)
val centi = UnitScale(1e-2)
val deci = UnitScale(1e-1)
val uni = UnitScale("", 1)
val deca = UnitScale("da", 10)
val hecto = UnitScale(100)
val kilo = UnitScale(1_000)
val mega = UnitScale(1_000_000)
val giga = UnitScale(1_000_000_000)
val tera = UnitScale(1_000_000_000_000)
val peta = UnitScale(1_000_000_000_000_000)
val exa = UnitScale(1_000_000_000_000_000_000)

val ki = UnitScale(1_024)
val me = UnitScale(1_048_576)
val gi = UnitScale(1_073_741_824)
val te = UnitScale(1_099_511_627_776)
val pi = UnitScale(1_125_899_906_842_624)
val ei = UnitScale(1_152_921_504_606_846_976)
