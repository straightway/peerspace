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

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

abstract class ProductOfBaseQuantities_StringRepBaseTest(private val stringGetter: Quantity.() -> String) {

    @Test fun oneUnitProduct() =
        assertEquals("", oneUnitProduct.stringGetter())

    @Test fun only_linear_AmountOfSubstance() =
        assertEquals("mol", ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, NoM, NoTheta, NoT).stringGetter())

    @Test fun only_linear_ElectricCurrent() =
        assertEquals("A", ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, NoM, NoTheta, NoT).stringGetter())

    @Test fun only_linear_Length() =
        assertEquals("m", ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, NoM, NoTheta, NoT).stringGetter())

    @Test fun only_linear_LuminousIntensity() =
        assertEquals("cd", ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), NoM, NoTheta, NoT).stringGetter())

    @Test fun only_linear_Temperature() =
        assertEquals("K", ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, linear(kelvin), NoT).stringGetter())

    @Test fun only_linear_Time() =
        assertEquals("s", ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, NoTheta, linear(second)).stringGetter())

    @Test fun only_reciprokeLinear_Length() =
        assertEquals("1/m", ProductOfBaseQuantities(NoN, NoI, reciproke(linear(meter)), NoJ, NoM, NoTheta, NoT).stringGetter())

    @Test fun linear_allUnits() =
        assertEquals(
            "mol*A*m*cd*kg*K*s",
            ProductOfBaseQuantities(
                linear(mol),
                linear(ampere),
                linear(meter),
                linear(candela),
                linear(kilo(gramm)),
                linear(kelvin),
                linear(second)).stringGetter())

    @Test fun square_allUnits() =
        assertEquals(
            "mol²*A²*m²*cd²*(kg)²*K²*s²",
            ProductOfBaseQuantities(
                square(mol),
                square(ampere),
                square(meter),
                square(candela),
                square(kilo(gramm)),
                square(kelvin),
                square(second)).stringGetter())

    @Test fun reciprokeLinear_allUnits() =
        assertEquals(
            "1/(mol*A*m*cd*kg*K*s)",
            ProductOfBaseQuantities(
            reciproke(linear(mol)),
            reciproke(linear(ampere)),
            reciproke(linear(meter)),
            reciproke(linear(candela)),
            reciproke(linear(kilo(gramm))),
            reciproke(linear(kelvin)),
            reciproke(linear(second))).stringGetter())

    @Test fun mixed_allUnits() =
        assertEquals(
            "mol*m*kg*s³/(A*cd*K²)",
            ProductOfBaseQuantities(
                linear(mol),
                reciproke(linear(ampere)),
                linear(meter),
                reciproke(linear(candela)),
                linear(kilo(gramm)),
                reciproke(square(kelvin)),
                cubic(second)).stringGetter())
}