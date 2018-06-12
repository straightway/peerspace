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

import org.koin.Koin
import org.koin.KoinContext
import org.koin.core.bean.BeanRegistry
import org.koin.core.instance.InstanceFactory
import org.koin.core.property.PropertyRegistry
import org.koin.dsl.context.Context

/**
 * Instantiate KoinModuleComponent instances specifying a context with beans,
 * factories and properties.
 * @see KoinModuleComponent
 */
class WithModules(private val modules: Iterable<IndependentModule>) {

    var propertiesFile: String? = "/koin.properties"
    var isUsingEnvironmentProperties = false
    val extraProperties: MutableMap<String, Any> = HashMap()

    infix fun <T> make(init: KoinContext.() -> T): T {
        return withContext {
            val context = build(modules)
            context.loadProperties()
            init()
        }
    }

    private fun Koin.loadProperties() {
        propertiesFile.let { if (it !== null) bindKoinProperties(it) }
        if (extraProperties.isNotEmpty()) bindAdditionalProperties(extraProperties)
        if (isUsingEnvironmentProperties) bindEnvironmentProperties()
    }

    companion object {
        operator fun invoke(vararg modules: IndependentModule) =
                WithModules(modules.toList())

        private fun <T> withContext(action: KoinContext.() -> T): T {
            val threadSpecificContext = KoinContext(
                    BeanRegistry(),
                    PropertyRegistry(),
                    InstanceFactory())

            val oldContext = if (KoinModuleComponent.hasContext)
                KoinModuleComponent.currentContext else null
            KoinModuleComponent.currentContext = threadSpecificContext
            try {
                return threadSpecificContext.action()
            } finally {
                KoinModuleComponent.currentContext = oldContext
            }
        }

        private fun KoinContext.build(modules: Iterable<IndependentModule>) =
                Koin(this).apply { build(modules bindTo this@build) }

        private infix fun Iterable<IndependentModule>.bindTo(context: KoinContext) =
                map { it bindTo context }

        private infix fun IndependentModule.bindTo(context: KoinContext) =
                { this(context) }
    }
}

fun withContext(init: Context.() -> Unit) = WithModules(independentContext(init))