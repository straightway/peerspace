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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class AngleTest {
    @Test fun radian_id() = Assertions.assertEquals(one.id, (radian).id)
    @Test fun radian_toString() = Assertions.assertEquals("rad", ((radian).toString()))
    @Test fun radian_scaling() = Assertions.assertEquals(1000[radian], 1[kilo(radian)])

    @Test fun degree_id() = Assertions.assertEquals(one.id, (degree).id)
    @Test fun degree_toString() = Assertions.assertEquals("°", ((degree).toString()))
    @Test fun degree_scaling() = Assertions.assertEquals(1000[degree], 1[kilo(degree)])
    @Test fun conversion_degree_rad() = Assertions.assertEquals(Math.PI, 180[degree][radian].value)
    @Test fun conversion_rad_degree() = Assertions.assertEquals(180.0, Math.PI[radian][degree].value)
}