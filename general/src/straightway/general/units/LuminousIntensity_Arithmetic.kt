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

// Replace Regex: J([0-9a-uw-zA-Z]+)

private typealias JProduct<J, I, L, N, M, Theta, T> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t3, t1, t2, t0, t4, t5, t6)

private typealias JFactor = LuminousIntensity
private typealias JQ1 = ElectricCurrent
private typealias JQ2 = Length
private typealias JQ3 = AmountOfSubstance
private typealias JQ4 = Mass
private typealias JQ5 = Temperature
private typealias JQ6 = Time

private val noQ0 = NoJ

private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = j
private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = i
private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = l
private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = n
private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = m
private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = theta
private val <T0: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = t

@JvmName("ProductOfBaseQuantities_None_Times_LuminousIntensity")
operator fun <T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<None<JFactor>, T1, T2, T3, T4, T5, T6>.times(f: JFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_LuminousIntensity")
operator fun
    JProduct<None<JFactor>, None<JQ1>, None<JQ2>, None<JQ3>, None<JQ4>, None<JQ5>, None<JQ6>>.times(f: JFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_LuminousIntensity")
operator fun <T0: Factor<JFactor, NRest>, NRest: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: JFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_LuminousIntensity")
operator fun <T0: Reciproke<JFactor, Linear<JFactor>>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: JFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_LuminousIntensity")
operator fun <T0: Reciproke<JFactor, Factor<JFactor, Factor<JFactor, TRest>>>, TRest: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: JFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_LuminousIntensity")
operator fun <T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<None<JFactor>, T1, T2, T3, T4, T5, T6>.div(f: JFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_LuminousIntensity")
operator fun <T0: Linear<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: JFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_LuminousIntensity")
operator fun
    JProduct<Square<JFactor>, None<JQ1>, None<JQ2>, None<JQ3>, None<JQ4>, None<JQ5>, None<JQ6>>.div(f: JFactor) =
    f.withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Factor_Div_LuminousIntensity")
operator fun <T0: Factor<JFactor, Factor<JFactor, TRest>>, TRest: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: JFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_LuminousIntensity")
operator fun <T0: Reciproke<JFactor, TRest>, TRest: QuantityExpr<JFactor>, T1: QuantityExpr<JQ1>, T2: QuantityExpr<JQ2>, T3: QuantityExpr<JQ3>, T4: QuantityExpr<JQ4>, T5: QuantityExpr<JQ5>, T6: QuantityExpr<JQ6>>
    JProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: JFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
