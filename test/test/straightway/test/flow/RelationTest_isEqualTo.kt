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
package straightway.test.flow

import org.junit.jupiter.api.Test
import straightway.general.dsl.minus
import straightway.test.assertDoesNotThrow
import straightway.test.assertFails

class RelationTest_isEqualTo {

    @Test fun passes() = assertDoesNotThrow { expect(1 _is equal to 1) }
    @Test fun fails() = assertFails { expect(1 _is equal to 2) }
    @Test fun negation_passes() = assertDoesNotThrow { expect(1 _is not-equal to 2) }
}