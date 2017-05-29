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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class ProductOfBaseQuantities_shortId_Test : ProductOfBaseQuantities_StringRepBaseTest({ shortId }) {

    @Test fun only_linear_Mass() =
        Assertions.assertEquals("kg", ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, linear(gramm), NoTheta, NoT).shortId)

    @Test fun withScale() =
        Assertions.assertEquals(
            "A*kg/s²",
            ProductOfBaseQuantities(
                NoN,
                linear(mega(ampere)),
                NoL,
                NoJ,
                linear(kilo(gramm)),
                NoTheta,
                reciproke(square(milli(second)))).shortId)
}