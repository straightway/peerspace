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

private typealias TProduct<T, I, L, J, M, Theta, N> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t6, t1, t2, t3, t4, t5, t0)

private typealias TFactor = Time
private typealias TQ1 = ElectricCurrent
private typealias TQ2 = Length
private typealias TQ3 = LuminousIntensity
private typealias TQ4 = Mass
private typealias TQ5 = Temperature
private typealias TQ6 = AmountOfSubstance

private val noQ0 = NoT

private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = t
private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = i
private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = l
private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = j
private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = m
private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = theta
private val <T0: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = n

@JvmName("ProductOfBaseQuantities_None_Times_Time")
operator fun <T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<None<TFactor>, T1, T2, T3, T4, T5, T6>.times(f: TFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_Time")
operator fun
    TProduct<None<TFactor>, None<TQ1>, None<TQ2>, None<TQ3>, None<TQ4>, None<TQ5>, None<TQ6>>.times(f: TFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_Time")
operator fun <T0: Factor<TFactor, NRest>, NRest: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: TFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_Time")
operator fun <T0: Reciproke<TFactor, Linear<TFactor>>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: TFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_Time")
operator fun <T0: Reciproke<TFactor, Factor<TFactor, Factor<TFactor, TRest>>>, TRest: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: TFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_Time")
operator fun <T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<None<TFactor>, T1, T2, T3, T4, T5, T6>.div(f: TFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_Time")
operator fun <T0: Linear<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: TFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_Time")
operator fun
    TProduct<Square<TFactor>, None<TQ1>, None<TQ2>, None<TQ3>, None<TQ4>, None<TQ5>, None<TQ6>>.div(f: TFactor) =
    f.withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Factor_Div_Time")
operator fun <T0: Factor<TFactor, Factor<TFactor, TRest>>, TRest: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: TFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_Time")
operator fun <T0: Reciproke<TFactor, TRest>, TRest: QuantityExpr<TFactor>, T1: QuantityExpr<TQ1>, T2: QuantityExpr<TQ2>, T3: QuantityExpr<TQ3>, T4: QuantityExpr<TQ4>, T5: QuantityExpr<TQ5>, T6: QuantityExpr<TQ6>>
    TProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: TFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
