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

private val AmountOfSubstance.prod get() = ProductOfBaseQuantities(linear(this), NoI, NoL, NoJ, NoM, NoTheta, NoT)
private val ElectricCurrent.prod get() = ProductOfBaseQuantities(NoN, linear(this), NoL, NoJ, NoM, NoTheta, NoT)
private val Length.prod get() = ProductOfBaseQuantities(NoN, NoI, linear(this), NoJ, NoM, NoTheta, NoT)
private val LuminousIntensity.prod get() = ProductOfBaseQuantities(NoN, NoI, NoL, linear(this), NoM, NoTheta, NoT)
private val Mass.prod get() = ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, linear(this), NoTheta, NoT)
private val Temperature.prod get() = ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, linear(this), NoT)
private val Time.prod get() = ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, NoTheta, linear(this))

operator fun AmountOfSubstance.times(f: AmountOfSubstance) = prod * f
operator fun AmountOfSubstance.times(f: ElectricCurrent) = prod * f
operator fun AmountOfSubstance.times(f: Length) = prod * f
operator fun AmountOfSubstance.times(f: LuminousIntensity) = prod * f
operator fun AmountOfSubstance.times(f: Mass) = prod * f
operator fun AmountOfSubstance.times(f: Temperature) = prod * f
operator fun AmountOfSubstance.times(f: Time) = prod * f

operator fun ElectricCurrent.times(f: AmountOfSubstance) = f * this
operator fun ElectricCurrent.times(f: ElectricCurrent) = prod * f
operator fun ElectricCurrent.times(f: Length) = prod * f
operator fun ElectricCurrent.times(f: LuminousIntensity) = prod * f
operator fun ElectricCurrent.times(f: Mass) = prod * f
operator fun ElectricCurrent.times(f: Temperature) = prod * f
operator fun ElectricCurrent.times(f: Time) = prod * f

operator fun Length.times(f: AmountOfSubstance) = f * this
operator fun Length.times(f: ElectricCurrent) = f * this
operator fun Length.times(f: Length) = prod * f
operator fun Length.times(f: LuminousIntensity) = prod * f
operator fun Length.times(f: Mass) = prod * f
operator fun Length.times(f: Temperature) = prod * f
operator fun Length.times(f: Time) = prod * f

operator fun LuminousIntensity.times(f: AmountOfSubstance) = f * this
operator fun LuminousIntensity.times(f: ElectricCurrent) = f * this
operator fun LuminousIntensity.times(f: Length) = f * this
operator fun LuminousIntensity.times(f: LuminousIntensity) = prod * f
operator fun LuminousIntensity.times(f: Mass) = prod * f
operator fun LuminousIntensity.times(f: Temperature) = prod * f
operator fun LuminousIntensity.times(f: Time) = prod * f

operator fun Mass.times(f: AmountOfSubstance) = f * this
operator fun Mass.times(f: ElectricCurrent) = f * this
operator fun Mass.times(f: Length) = f * this
operator fun Mass.times(f: LuminousIntensity) = f * this
operator fun Mass.times(f: Mass) = prod * f
operator fun Mass.times(f: Temperature) = prod * f
operator fun Mass.times(f: Time) = prod * f

operator fun Temperature.times(f: AmountOfSubstance) = f * this
operator fun Temperature.times(f: ElectricCurrent) = f * this
operator fun Temperature.times(f: Length) = f * this
operator fun Temperature.times(f: LuminousIntensity) = f * this
operator fun Temperature.times(f: Mass) = f * this
operator fun Temperature.times(f: Temperature) = prod * f
operator fun Temperature.times(f: Time) = prod * f

operator fun Time.times(f: AmountOfSubstance) = f * this
operator fun Time.times(f: ElectricCurrent) = f * this
operator fun Time.times(f: Length) = f * this
operator fun Time.times(f: LuminousIntensity) = f * this
operator fun Time.times(f: Mass) = f * this
operator fun Time.times(f: Temperature) = f * this
operator fun Time.times(f: Time) = prod * f
