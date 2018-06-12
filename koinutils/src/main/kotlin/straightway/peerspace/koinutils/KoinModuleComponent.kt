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
import org.koin.core.parameter.Parameters
import org.koin.dsl.context.emptyParameters
import org.koin.error.MissingPropertyException

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

inline fun <reified T> KoinModuleComponent.inject(name: String = "") = kotlin.lazy {
    get<T>(name, emptyParameters())
}

inline fun <reified T> KoinModuleComponent.inject(
        name: String,
        noinline parameters: Parameters
) = kotlin.lazy { get<T>(name, parameters) }

inline fun <reified T> KoinModuleComponent.inject(
        noinline parameters: Parameters
) = kotlin.lazy { get<T>("", parameters) }

inline fun <reified T> KoinModuleComponent.property(key: String) =
        kotlin.lazy { getProperty<T>(key) }

inline fun <reified T> KoinModuleComponent.property(key: String, default: T) =
        kotlin.lazy { getProperty(key, default) }

inline fun <reified T> KoinModuleComponent.property(
        key: String,
        noinline converter: (String) -> T) =
        kotlin.lazy { getProperty(key, converter) }

inline fun <reified T> KoinModuleComponent.property(
        key: String,
        default: T,
        noinline converter: (String) -> T) =
        kotlin.lazy { getProperty(key, default, converter) }

inline fun <reified T> KoinModuleComponent.get(name: String = "") =
        withOwnContext { get<T>(name, emptyParameters()) }

inline fun <reified T> KoinModuleComponent.get(
        name: String,
        noinline parameters: Parameters
) = withOwnContext { get<T>(name, parameters) }

inline fun <reified T> KoinModuleComponent.get(noinline parameters: Parameters) =
        withOwnContext { get<T>("", parameters) }

inline fun <reified T> KoinModuleComponent.getProperty(key: String) =
        context.getProperty<T>(key)

inline fun <reified T> KoinModuleComponent.getProperty(key: String, defaultValue: T) =
        context.getProperty(key, defaultValue)

inline fun <reified T> KoinModuleComponent.getProperty(
        key: String,
        noinline converter: (String) -> T) =
        converter(context.getProperty(key))

inline fun <reified T> KoinModuleComponent.getProperty(
        key: String,
        default: T,
        noinline converter: (String) -> T) =
        try {
            converter(context.getProperty(key))
        } catch (e: MissingPropertyException) {
            default
        }

fun KoinModuleComponent.releaseProperties(vararg keys: String) =
        context.releaseProperties(*keys)

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