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

class ProductOfBaseQuantities_Scale_Test {

    @Test fun one_hasUnitScale() =
        assertEquals(uni, oneUnitProduct.scale)

    @Test fun allBaseUnits_hasUnitScale() =
        assertEquals(
            uni,
            ProductOfBaseQuantities(
                linear(mol),
                linear(ampere),
                linear(meter),
                linear(candela),
                linear(kilo(gramm)),
                linear(kelvin),
                linear(second)).scale)

    @Test fun singleBaseUnits_hasSameScale() =
        assertEquals(
            kilo,
            ProductOfBaseQuantities(
                linear(kilo(mol)),
                NoI,
                NoL,
                NoJ,
                NoM,
                NoTheta,
                NoT).scale)

    @Test fun singleReciprokeBaseUnits_hasReciprokeScale() =
        assertEquals(
            milli,
            ProductOfBaseQuantities(
                reciproke(linear(kilo(mol))),
                NoI,
                NoL,
                NoJ,
                NoM,
                NoTheta,
                NoT).scale)

    @Test fun allBaseUnitsLinear_hasCombinedScale() =
        assertEquals(
            milli,
            ProductOfBaseQuantities(
                linear(mega(mol)),
                linear(micro(ampere)),
                linear(milli(meter)),
                linear(kilo(candela)),
                linear(gramm),
                linear(kilo(kelvin)),
                linear(milli(second))).scale)

    @Test fun notPredefinedCombinedScale() =
        assertEquals(
            UnitScale(10000),
            ProductOfBaseQuantities(
                linear(kilo(mol)),
                linear(deca(ampere)),
                NoL,
                NoJ,
                NoM,
                NoTheta,
                NoT).scale)
}