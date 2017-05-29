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

class ElectricCurrent_Arithmetic_Test {

    private fun <Q: EI> product1(f: Q) =
        ProductOfBaseQuantities(NoN, f, NoL, NoJ, NoM, NoTheta, NoT)
    private fun <Q: EI> product2(f: Q) =
        ProductOfBaseQuantities(linear(mol), f, NoL, NoJ, NoM, NoTheta, NoT)
    private val factor = ampere

    private val testScale = factor.siScaleCorrection * kilo

    @Test fun times_differentBaseUnit() =
        assertEquals(product2(linear(factor)), product2(None(factor)) * factor)

    @Test fun times_differentBaseUnit_leftScaled() =
        assertEquals(kilo(product2(linear(factor))), kilo(product2(None(factor))) * factor)

    @Test fun times_differentBaseUnit_rightScaled() =
        assertEquals(kilo(product2(linear(factor))), product2(None(factor)) * testScale(factor))

    @Test fun times_one() =
        assertEquals(factor, oneUnitProduct * factor)

    @Test fun times_one_leftScaled() =
        assertEquals(testScale(factor), kilo(oneUnitProduct) * factor)

    @Test fun times_one_rightScaled() =
        assertEquals(testScale(factor), oneUnitProduct * testScale(factor))

    @Test fun times_linear() =
        assertEquals(product1(square(factor)), product1(linear(factor)) * factor)

    @Test fun times_linear_leftScaled() =
        assertEquals(kilo(product1(square(factor))), kilo(product1(linear(factor))) * factor)

    @Test fun times_linear_rightScaled() =
        assertEquals(kilo(product1(square(factor))), product1(linear(factor)) * testScale(factor))

    @Test fun times_square() =
        assertEquals(product1(cubic(factor)), product1(square(factor)) * factor)

    @Test fun times_square_leftScaled() =
        assertEquals(kilo(product1(cubic(factor))), kilo(product1(square(factor))) * factor)

    @Test fun times_square_rightScaled() =
        assertEquals(kilo(product1(cubic(factor))), product1(square(factor)) * testScale(factor))

    @Test fun times_recicprokeLinear() =
        assertEquals(oneUnitProduct, product1(reciproke(linear(factor))) * factor)

    @Test fun times_recicprokeLinear_leftScaled() =
        assertEquals(kilo(oneUnitProduct), kilo(product1(reciproke(linear(factor)))) * factor)

    @Test fun times_recicprokeLinear_rightScaled() =
        assertEquals(kilo(oneUnitProduct), product1(reciproke(linear(factor))) * testScale(factor))

    @Test fun times_recicprokeSquare() =
        assertEquals(product1(reciproke(linear(factor))), product1(reciproke(square(factor))) * factor)

    @Test fun times_recicprokeSquare_leftScaled() =
        assertEquals(kilo(product1(reciproke(linear(factor)))), kilo(product1(reciproke(square(factor)))) * factor)

    @Test fun times_recicprokeSquare_rightScaled() =
        assertEquals(kilo(product1(reciproke(linear(factor)))), product1(reciproke(square(factor))) * testScale(factor))

    @Test fun times_recicprokeCubic() =
        assertEquals(product1(reciproke(square(factor))), product1(reciproke(cubic(factor))) * factor)

    @Test fun times_recicprokeCubic_leftScaled() =
        assertEquals(kilo(product1(reciproke(square(factor)))), kilo(product1(reciproke(cubic(factor)))) * factor)

    @Test fun times_recicprokeCubic_rightScaled() =
        assertEquals(kilo(product1(reciproke(square(factor)))), product1(reciproke(cubic(factor))) * testScale(factor))

    @Test fun one_div() =
        assertEquals(product1(reciproke(linear(factor))), oneUnitProduct / factor)

    @Test fun one_div_leftScaled() =
        assertEquals(kilo(product1(reciproke(linear(factor)))), kilo(oneUnitProduct) / factor)

    @Test fun one_div_rightScaled() =
        assertEquals(milli(product1(reciproke(linear(factor)))), oneUnitProduct / testScale(factor))

    @Test fun linear_div() =
        assertEquals(oneUnitProduct, product1(linear(factor)) / factor)

    @Test fun linear_div_leftScaled() =
        assertEquals(kilo(oneUnitProduct), kilo(product1(linear(factor))) / factor)

    @Test fun linear_div_rightScaled() =
        assertEquals(milli(oneUnitProduct), product1(linear(factor)) / testScale(factor))

    @Test fun square_withOtherUnit_div() =
        assertEquals(product2(linear(factor)), product2(square(factor)) / factor)

    @Test fun square_withOtherUnit_div_leftScaled() =
        assertEquals(kilo(product2(linear(factor))), kilo(product2(square(factor))) / factor)

    @Test fun square_withOtherUnit_div_rightScaled() =
        assertEquals(milli(product2(linear(factor))), product2(square(factor)) / testScale(factor))

    @Test fun square_div() =
        assertEquals(factor, product1(square(factor)) / factor)

    @Test fun square_div_leftScaled() =
        assertEquals(testScale(factor), kilo(product1(square(factor))) / factor)

    @Test fun square_div_rightScaled() =
        assertEquals((milli * factor.siScaleCorrection)(factor), product1(square(factor)) / testScale(factor))

    @Test fun cubic_div() =
        assertEquals(product1(square(factor)), product1(cubic(factor)) / factor)

    @Test fun cubic_div_leftScaled() =
        assertEquals(kilo(product1(square(factor))), kilo(product1(cubic(factor))) / factor)

    @Test fun cubic_div_rightScaled() =
        assertEquals(milli(product1(square(factor))), product1(cubic(factor)) / testScale(factor))

    @Test fun reciprokeLinear_div() =
        assertEquals(product1(reciproke(square(factor))), product1(reciproke(linear(factor))) / factor)

    @Test fun reciprokeLinear_div_leftScaled() =
        assertEquals(kilo(product1(reciproke(square(factor)))), kilo(product1(reciproke(linear(factor)))) / factor)

    @Test fun reciprokeLinear_div_rightScaled() =
        assertEquals(milli(product1(reciproke(square(factor)))), product1(reciproke(linear(factor))) / testScale(factor))

    @Test fun reciprokeSquare_div() =
        assertEquals(product1(reciproke(cubic(factor))), product1(reciproke(square(factor))) / factor)

    @Test fun reciprokeSquare_div_leftScaled() =
        assertEquals(kilo(product1(reciproke(cubic(factor)))), kilo(product1(reciproke(square(factor)))) / factor)

    @Test fun reciprokeSquare_div_rightScaled() =
        assertEquals(milli(product1(reciproke(cubic(factor)))), product1(reciproke(square(factor))) / testScale(factor))
}