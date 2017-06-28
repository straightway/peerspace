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
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class QuantityTest {
    @Test fun siScale_withoutSiScaleCorrection_unscaled() =
        assertEquals(uni, candela.siScale)
    @Test fun siScale_withoutSiScaleCorrection_scaled() =
        assertEquals(kilo, kilo(candela).siScale)
    @Test fun siScale_withSiScaleCorrection_unscaled() =
        assertEquals(milli, gramm.siScale)
    @Test fun siScale_withSiScaleCorrection_scaledToSiScaleCorrection() =
        assertEquals(uni, kilo(gramm).siScale)
    @Test fun siScale_withSiScaleCorrection_upscaled() =
        assertEquals(kilo, mega(gramm).siScale)
    @Test fun timesScaleOf_considersReceiverScale() =
        assertEquals(kilo(meter), kilo(meter).timesScaleOf(mol))
    @Test fun timesScaleOf_considersParameterScale() =
        assertEquals(kilo(meter), meter.timesScaleOf(kilo(mol)))
    @Test fun timesScaleOf_considersBothScales() =
        assertEquals(mega(meter), kilo(meter).timesScaleOf(kilo(mol)))
    @Test fun timesScaleOf_considersReceiverSiScaleCorrection() =
        assertEquals(kilo(gramm), kilo(gramm).timesScaleOf(mol))
    @Test fun timesScaleOf_considersParametersSiScaleCorrection() =
        assertEquals(meter, meter.timesScaleOf(kilo(gramm)))
    @Test fun timesScaleOf_considersBothSiScaleCorrections() =
        assertEquals(kilo(gramm), kilo(gramm).timesScaleOf(kilo(gramm)))

    @Test fun timesScaleOf_uni_returnsReceiver() {
        val sut = kilo(meter)
        assertSame(sut, sut.timesScaleOf(mol))
    }

    @Test fun timesScaleOf_correctedUni_returnsReceiver() {
        val sut = kilo(meter)
        assertSame(sut, sut.timesScaleOf(kilo(gramm)))
    }
    @Test fun divScaleOf_considersReceiverScale() =
        assertEquals(kilo(meter), kilo(meter).divScaleOf(mol))
    @Test fun divScaleOf_considersParameterScale() =
        assertEquals(milli(meter), meter.divScaleOf(kilo(mol)))
    @Test fun divScaleOf_considersBothScales() =
        assertEquals(meter, kilo(meter).divScaleOf(kilo(mol)))
    @Test fun divScaleOf_considersReceiverSiScaleCorrection() =
        assertEquals(kilo(gramm), kilo(gramm).divScaleOf(meter))
    @Test fun divScaleOf_considersParametersSiScaleCorrection() =
        assertEquals(meter, meter.divScaleOf(kilo(gramm)))
    @Test fun divScaleOf_considersBothSiScaleCorrections() =
        assertEquals(kilo(gramm), kilo(gramm).divScaleOf(kilo(gramm)))

    @Test fun divScaleOf_uni_returnsReceiver() {
        val sut = kilo(meter)
        assertSame(sut, sut.divScaleOf(mol))
    }

    @Test fun divScaleOf_correctedUni_returnsReceiver() {
        val sut = kilo(meter)
        assertSame(sut, sut.divScaleOf(kilo(gramm)))
    }
}