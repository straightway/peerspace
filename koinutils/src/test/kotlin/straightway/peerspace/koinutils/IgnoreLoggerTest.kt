/*
 * Copyright 2016 github.com/straightway
 *
 *  Licensed under the Apache License, Version 2.0 (the &quot;License&quot;);
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package straightway.peerspace.koinutils

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import straightway.testing.bdd.Given
import straightway.testing.flow.Equal
import straightway.testing.flow.expect
import straightway.testing.flow.is_
import straightway.testing.flow.to_
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class IgnoreLoggerTest {

    private val test get() = Given { IgnoreLogger() }

    private lateinit var outContent: ByteArrayOutputStream

    @BeforeEach
    fun setup() {
        outContent = ByteArrayOutputStream()
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(outContent))
    }

    @AfterEach
    fun tearDown() {
        System.setOut(System.out)
        System.setErr(System.err)
    }

    @Test
    fun `debug does nothing`() =
            test when_ { debug("Hello") } then {
                expect(outContent.size() is_ Equal to_ 0)
            }

    @Test
    fun `log does nothing`() =
            test when_ { log("Hello") } then {
                expect(outContent.size() is_ Equal to_ 0)
            }

    @Test
    fun `err does nothing`() =
            test when_ { err("Hello") } then {
                expect(outContent.size() is_ Equal to_ 0)
            }
}