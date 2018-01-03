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
package straightway.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NonMetricForceAndPressureTest {

    @Test fun compare_poundForce_to_newton() =
        assertEquals(4.4482216152605[newton], 1[poundForce])

    @Test fun compare_psi_to_pascal() =
        assertEquals(6894.757293168362[pascal].baseValue, 1.0[psi].baseValue)

    @Test fun compare_bar_pascal() =
        assertEquals(1000[hecto(pascal)], 1[bar])
}