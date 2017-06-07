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

class ExponentiationTest {

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

    @Test fun scale_firstArgument() =
        assertEquals(kilo, Product(kilo(meter), meter).scale)
    @Test fun scale_secondArgument() =
        assertEquals(kilo, Product(meter, kilo(meter)).scale)
    @Test fun scale_bothArguments() =
        assertEquals(mega, Product(kilo(meter), kilo(meter)).scale)

    @Test fun scale_correctedScale_firstArgument() =
        assertEquals(milli, Product(kilo(gramm), gramm).scale)
    @Test fun scale_correctedScale_secondArgument() =
        assertEquals(milli, Product(gramm, kilo(gramm)).scale)
    @Test fun scale_correctedScale_bothArguments() =
        assertEquals(uni, Product(kilo(gramm), kilo(gramm)).scale)
}