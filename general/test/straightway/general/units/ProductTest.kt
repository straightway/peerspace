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

class ProductTest {

    @Test fun toString_containsBothSubExpressions() =
        assertEquals("m*s", Product(second, meter).toString())
    @Test fun toString_showsRescaledScale() =
        assertEquals("k(m*s)", kilo(Product(milli(second), milli(meter))).toString())
    @Test fun toString_unscaled_showsLeftScale() =
        assertEquals("m*ms", Product(milli(second), meter).toString())
    @Test fun toString_unscaled_showsRightScale() =
        assertEquals("mm*s", Product(second, milli(meter)).toString())
    @Test fun toString_unscaled_differentScale_sameUnit() =
        assertEquals("k(m²)", Product(mega(meter), milli(meter)).toString())
    @Test fun toString_separatesReciprokeFactors() =
        assertEquals("1/km*ms", Product(Reciproke(kilo(meter)), Reciproke(milli(second))).toString())
    @Test fun toString_withComplexNumeratorAndDenominator() =
        assertEquals(
            "Mmol*kK²/m²*ms",
            Product(kilo(kelvin), Product(mega(mol), Product(kilo(kelvin),
                Product(Reciproke(meter), Product(Reciproke(meter), Reciproke(milli(second))))))).toString())

    @Test fun shortId_containsBothSubExpressions() =
        assertEquals("m*s", Product(second, meter).shortId)
    @Test fun shortId_containsNoScale() =
        assertEquals("m*s", Product(kilo(second), kilo(meter)).shortId)
    @Test fun shortId_considersCorrectedSiScale() =
        assertEquals("kg*m", Product(kilo(gramm), kilo(meter)).shortId)
    @Test fun shortId_ofSquare_differentScale_sameUnit() =
        assertEquals("m²", Product(mega(meter), milli(meter)).shortId)
    @Test fun shortId_ofSquare_usesSquareCharacter() =
        assertEquals("m²", Product(meter, meter).shortId)
    @Test fun shortId_ofScaleCorrectedSquare_usesSquareCharacter() =
        assertEquals("kg²", Product(gramm, gramm).shortId)
    @Test fun shortId_ofCubic_usesCubicCharacter() =
        assertEquals("m³", Product(meter, Product(meter, meter)).shortId)
    @Test fun shortId_ofPow4_usesCaret() =
        assertEquals("m^4", Product(meter, Product(meter, Product(meter, meter))).shortId)
    @Test fun shortId_ofSquare_usesSquareCharacter_withinOtherProduct_left() =
        assertEquals("m²*s", Product(meter, Product(meter, second)).shortId)
    @Test fun shortId_ofSquare_usesSquareCharacter_withinOtherProduct_right() =
        assertEquals("m*s²", Product(Product(meter, second), second).shortId)
    @Test fun shortId_separatesReciprokeFactors() =
        assertEquals("1/m*s", Product(Reciproke(meter), Reciproke(second)).shortId)
    @Test fun shortId_withComplexNumeratorAndDenominator() =
        assertEquals(
            "K²*mol/m²*s",
            Product(kelvin, Product(mol, Product(kelvin,
                Product(Reciproke(meter), Product(Reciproke(meter), Reciproke(second)))))).shortId)

    @Test fun withShortId_changesShortId() =
        assertEquals("X", (meter / second).withShortId("X").shortId)

    @Test fun withShortId_changesStringRepresentation() =
        assertEquals("X", (meter / second).withShortId("X").toString())

    @Test fun withShortId_keepsScale() =
        assertEquals(deca, (kilo(meter) / hecto(second)).withShortId("X").scale)

    @Test fun withShortId_respectsScaleCorrection() =
        assertEquals(uni, (kilo(gramm) / second).withShortId("X").scale)

    @Test fun scale_uni_whenBothSubexpressionsHave_ScaleUni() =
        assertEquals(uni, Product(second, meter).scale)
    @Test fun scale_isLeftScale_whenRightIsUni() =
        assertEquals(kilo, Product(second, kilo(meter)).scale)
    @Test fun scale_isRightScale_whenLeftIsUni() =
        assertEquals(mega, Product(mega(second), meter).scale)
    @Test fun scale_isProductOfBothScalesScale() =
        assertEquals(giga, Product(mega(second), kilo(meter)).scale)
    @Test fun scale_ofSquare_differentScale_sameUnit() =
        assertEquals(kilo, Product(mega(meter), milli(meter)).scale)
    @Test fun scale_isProductOfBothScalesScale_withLeftScaleCorrection() =
        assertEquals(mega, Product(kilo(gramm), mega(second)).scale)
    @Test fun scale_isProductOfBothScalesScale_withRightScaleCorrection() =
        assertEquals(mega, Product(mega(second), kilo(gramm)).scale)

    @Test fun isScalable() =
        assertEquals(kilo, kilo(Product(mega(meter), second)).scale)

    @Test fun times_createsProduct() =
        assertEquals(Product(meter, second), meter * second)
    @Test fun times_self_createsSquare() =
        assertEquals(square(meter), meter * meter)
    @Test fun times_leftOne_yieldsRight() =
        assertEquals(meter, one * meter)
    @Test fun times_rightOne_yieldsLeft() =
        assertEquals(meter, meter * one)

    @Test fun div_createsProductWithReciproke() =
        assertEquals(Product(meter, reciproke(second)), meter / second)

    @Test fun square_hasSquaredShortId() =
        assertEquals("m²", square(meter).shortId)
    @Test fun square_hasSquaredShortId_withScale() =
        assertEquals("m²", square(kilo(meter)).shortId)
    @Test fun Mass_square_hasSquaredShortId_withScale() =
        assertEquals("kg²", square(kilo(gramm)).shortId)
    @Test fun square_hasSquaredScale() =
        assertEquals(mega, square(kilo(meter)).scale)
    @Test fun square_respectsCorrectedSiScale() =
        assertEquals(uni, square(kilo(gramm)).scale)
    @Test fun square_toString() =
        assertEquals("m²", square(meter).toString())
    @Test fun Mass_square_toString() =
        assertEquals("kg²", square(kilo(gramm)).toString())
    @Test fun square_toString_withScale() =
        assertEquals("km²", square(kilo(meter)).toString())

    @Test fun cubic_hasCubicShortId() =
        assertEquals("m³", cubic(meter).shortId)
    @Test fun cubic_hasSquaredScale() =
        assertEquals(giga, cubic(kilo(meter)).scale)
    @Test fun cubic_respectsCorrectedSiScale() =
        assertEquals(uni, cubic(kilo(gramm)).scale)
    @Test fun cubic_toString() =
        assertEquals("m³", cubic(meter).toString())
    @Test fun cubic_toString_withScale() =
        assertEquals("km³", cubic(kilo(meter)).toString())

    @Test fun pow4_hasShortIdToThePowerOf4() =
        assertEquals("m^4", Product(meter, cubic(meter)).shortId)
    @Test fun pow4_hasSquaredScale() =
        assertEquals(tera, Product(kilo(meter), cubic(kilo(meter))).scale)
    @Test fun pow4_respectsCorrectedSiScale() =
        assertEquals(uni, Product(kilo(gramm), cubic(kilo(gramm))).scale)
    @Test fun pow4_toString() =
        assertEquals("m^4", Product(meter, cubic(meter)).toString())
    @Test fun pow4_toString_withScale() =
        assertEquals("km^4", Product(kilo(meter), cubic(kilo(meter))).toString())
}