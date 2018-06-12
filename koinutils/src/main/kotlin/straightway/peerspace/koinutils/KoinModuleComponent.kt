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

import org.koin.KoinContext

/**
 * Base interface for Koin components having their own context (and thus don't depend on
 * global koinStart). The default way is to implement this interface by KoinModuleComponent().
 * Such classes must be instantiated with a given local Koin context.
 * @see WithModules
 * @see withContext
 * @see withOwnContext
 */
interface KoinModuleComponent {

    val context: KoinContext

    companion object {
        operator fun invoke(): KoinModuleComponent = Impl()

        private class Impl : KoinModuleComponent {
            override val context = KoinModuleComponent.currentContext!!
        }

        val hasContext get() = currentThreadContext.get() != null

        var currentContext: KoinContext?
            get() = currentThreadContext.get() ?: throw ConstructedWithoutKoinModulesException()
            set(new) = currentThreadContext.set(new)

        private var currentThreadContext = ThreadLocal<KoinContext?>()
    }
}

fun KoinModuleComponent.releaseContext(name: String) =
        context.releaseContext(name)

fun <T> KoinModuleComponent.withOwnContext(action: KoinContext.() -> T): T {
    val oldContext = if (KoinModuleComponent.hasContext)
        KoinModuleComponent.currentContext else null
    try {
        KoinModuleComponent.currentContext = context
        return context.action()
    } finally {
        KoinModuleComponent.currentContext = oldContext
    }
}