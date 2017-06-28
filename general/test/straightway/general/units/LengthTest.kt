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

class LengthTest {
    @Test fun meter_toString() = assertEquals("m", meter.toString())
    @Test fun meter_scaling() = assertEquals(1000[meter], 1[kilo(meter)])

    @Test fun inch_id() = assertEquals(meter.id, inch.id)
    @Test fun inch_toString() = assertEquals("\"", inch.toString())
    @Test fun inch_scaling() = assertEquals(1000[inch], 1[kilo(inch)])
    @Test fun conversion_inch_to_cm() = assertEquals(2.54, 1[inch][centi(meter)].value)
    @Test fun conversion_cm_to_inch() = assertEquals(1.0, 2.54[centi(meter)][inch].value.toDouble(), 1e-6)

    @Test fun foot_id() = assertEquals(meter.id, foot.id)
    @Test fun foot_toString() = assertEquals("ft", foot.toString())
    @Test fun foot_scaling() = assertEquals(1000[foot], 1[kilo(foot)])
    @Test fun conversion_foot_to_cm() = assertEquals(30.48, 1[foot][centi(meter)].value.toDouble(), 1e-6)
    @Test fun conversion_cm_to_foot() = assertEquals(1.0, 30.48[centi(meter)][foot].value.toDouble(), 1e-6)

    @Test fun yard_id() = assertEquals(meter.id, yard.id)
    @Test fun yard_toString() = assertEquals("yd", yard.toString())
    @Test fun yard_scaling() = assertEquals(1000[yard], 1[kilo(yard)])
    @Test fun conversion_yard_to_cm() = assertEquals(91.44, 1[yard][centi(meter)].value.toDouble(), 1e-6)
    @Test fun conversion_cm_to_yard() = assertEquals(1.0, 91.44[centi(meter)][yard].value.toDouble(), 1e-6)

    @Test fun mile_id() = assertEquals(meter.id, mile.id)
    @Test fun mile_toString() = assertEquals("mile", mile.toString())
    @Test fun mile_scaling() = assertEquals(1000[mile], 1[kilo(mile)])
    @Test fun conversion_mile_to_km() = assertEquals(1.609344, 1[mile][kilo(meter)].value.toDouble(), 1e-6)
    @Test fun conversion_km_to_mile() = assertEquals(1.0, 1.609344[kilo(meter)][mile].value.toDouble(), 1e-6)

    @Test fun nauticalMile_id() = assertEquals(meter.id, nauticalMile.id)
    @Test fun nauticalMile_toString() = assertEquals("NM", nauticalMile.toString())
    @Test fun nauticalMile_scaling() = assertEquals(1000[nauticalMile], 1[kilo(nauticalMile)])
    @Test fun conversion_nauticalMile_to_km() = assertEquals(1.852, 1[nauticalMile][kilo(meter)].value.toDouble(), 1e-6)
    @Test fun conversion_km_to_nauticalMile() = assertEquals(1.0, 1.852[kilo(meter)][nauticalMile].value.toDouble(), 1e-6)
}