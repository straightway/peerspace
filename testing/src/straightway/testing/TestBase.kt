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
package straightway.testing

import org.junit.jupiter.api.AfterEach

/**
 * Base class for unit tests test obects of type T.
 */
open class TestBase<T> {

    //<editor-fold desc="Setup/tear down">
    @AfterEach
    fun tearDown() {
        nullableSut = null
    }
    //</editor-fold>

    protected var sut: T
        get() = nullableSut!!
        set(value) {
            nullableSut = value
        }

    //<editor-fold desc="Private">
    private var nullableSut: T? = null
    //</editor-fold>
}