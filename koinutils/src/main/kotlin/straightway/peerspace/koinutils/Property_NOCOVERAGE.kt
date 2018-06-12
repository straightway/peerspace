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
@file:Suppress("MatchingDeclarationName")
package straightway.peerspace.koinutils

import org.koin.error.MissingPropertyException

/**
 * Access to properties, either by injection or by directly getting them.
 */
object Property {

    inline fun <reified T> KoinModuleComponent.property(key: String) =
            kotlin.lazy { getProperty<T>(key) }

    inline fun <reified T> KoinModuleComponent.property(key: String, default: T) =
            kotlin.lazy { getProperty(key, default) }

    inline fun <reified T> KoinModuleComponent.property(
            key: String,
            noinline converter: (String) -> T
    ) =
            kotlin.lazy { getProperty(key, converter) }

    @Suppress("LongParameterList")
    inline fun <reified T> KoinModuleComponent.property(
            key: String,
            default: T,
            noinline converter: (String) -> T
    ) =
            kotlin.lazy { getProperty(key, default, converter) }

    inline fun <reified T> KoinModuleComponent.getProperty(key: String) =
            context.getProperty<T>(key)

    inline fun <reified T> KoinModuleComponent.getProperty(key: String, defaultValue: T) =
            context.getProperty(key, defaultValue)

    inline fun <reified T> KoinModuleComponent.getProperty(
            key: String,
            noinline converter: (String) -> T
    ) =
            converter(context.getProperty(key))

    @Suppress("LongParameterList")
    inline fun <reified T> KoinModuleComponent.getProperty(
            key: String,
            default: T,
            noinline converter: (String) -> T
    ) =
            try {
                converter(context.getProperty(key))
            } catch (e: MissingPropertyException) {
                default
            }

    fun KoinModuleComponent.releaseProperties(vararg keys: String) =
            context.releaseProperties(*keys)
}