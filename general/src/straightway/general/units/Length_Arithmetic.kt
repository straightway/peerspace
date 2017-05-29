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

private typealias LProduct<L, I, N, J, M, Theta, T> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t2, t1, t0, t3, t4, t5, t6)

private typealias LFactor = Length
private typealias LQ1 = ElectricCurrent
private typealias LQ2 = AmountOfSubstance
private typealias LQ3 = LuminousIntensity
private typealias LQ4 = Mass
private typealias LQ5 = Temperature
private typealias LQ6 = Time

private val noQ0 = NoL

private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = l
private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = i
private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = n
private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = j
private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = m
private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = theta
private val <T0: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = t

@JvmName("ProductOfBaseQuantities_None_Times_Length")
operator fun <T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<None<LFactor>, T1, T2, T3, T4, T5, T6>.times(f: LFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_Length")
operator fun
    LProduct<None<LFactor>, None<LQ1>, None<LQ2>, None<LQ3>, None<LQ4>, None<LQ5>, None<LQ6>>.times(f: LFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_Length")
operator fun <T0: Factor<LFactor, NRest>, NRest: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: LFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_Length")
operator fun <T0: Reciproke<LFactor, Linear<LFactor>>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: LFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_Length")
operator fun <T0: Reciproke<LFactor, Factor<LFactor, Factor<LFactor, TRest>>>, TRest: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: LFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_Length")
operator fun <T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<None<LFactor>, T1, T2, T3, T4, T5, T6>.div(f: LFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_Length")
operator fun <T0: Linear<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: LFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_Length")
operator fun
    LProduct<Square<LFactor>, None<LQ1>, None<LQ2>, None<LQ3>, None<LQ4>, None<LQ5>, None<LQ6>>.div(f: LFactor) =
    f.withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Factor_Div_Length")
operator fun <T0: Factor<LFactor, Factor<LFactor, TRest>>, TRest: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: LFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_Length")
operator fun <T0: Reciproke<LFactor, TRest>, TRest: QuantityExpr<LFactor>, T1: QuantityExpr<LQ1>, T2: QuantityExpr<LQ2>, T3: QuantityExpr<LQ3>, T4: QuantityExpr<LQ4>, T5: QuantityExpr<LQ5>, T6: QuantityExpr<LQ6>>
    LProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: LFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
