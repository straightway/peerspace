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
package straightway.general

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class PanicTest {

    @Test fun toString_containsState()
    {
        val sut = Panic(123);
        assertEquals("Panic: 123", sut.toString())
    }

    @Test fun isThrowable()
    {
        try {
            throw Panic("Aaaargh!")
        }
        catch (panic: Panic)
        {
            assertEquals("Panic: Aaaargh!", panic.toString())
        }
    }

    @Test fun state_isAccessible()
    {
        val state = Any()
        val sut = Panic(state)
        assertSame(state, sut.state)
    }
}