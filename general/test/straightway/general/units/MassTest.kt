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

class MassTest {

    @Test fun kiloGramm_isBaseUnit() = assertEquals(1.0, 1.0[kilo(gramm)].scaledValue)
    @Test fun gramm() = assertEquals(1e-3, 1.0[gramm].scaledValue)
    @Test fun shortd() = assertEquals("kg", gramm.shortId)
}