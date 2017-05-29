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

private typealias ThetaProduct<Theta, I, L, J, M, N, T> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t5, t1, t2, t3, t4, t0, t6)

private typealias ThetaFactor = Temperature
private typealias ThetaQ1 = ElectricCurrent
private typealias ThetaQ2 = Length
private typealias ThetaQ3 = LuminousIntensity
private typealias ThetaQ4 = Mass
private typealias ThetaQ5 = AmountOfSubstance
private typealias ThetaQ6 = Time

private val noQ0 = NoTheta

private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = theta
private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = i
private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = l
private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = j
private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = m
private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = n
private val <T0: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = t

@JvmName("ProductOfBaseQuantities_None_Times_Temperature")
operator fun <T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<None<ThetaFactor>, T1, T2, T3, T4, T5, T6>.times(f: ThetaFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_Temperature")
operator fun
    ThetaProduct<None<ThetaFactor>, None<ThetaQ1>, None<ThetaQ2>, None<ThetaQ3>, None<ThetaQ4>, None<ThetaQ5>, None<ThetaQ6>>.times(f: ThetaFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_Temperature")
operator fun <T0: Factor<ThetaFactor, NRest>, NRest: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: ThetaFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_Temperature")
operator fun <T0: Reciproke<ThetaFactor, Linear<ThetaFactor>>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: ThetaFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_Temperature")
operator fun <T0: Reciproke<ThetaFactor, Factor<ThetaFactor, Factor<ThetaFactor, TRest>>>, TRest: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: ThetaFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_Temperature")
operator fun <T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<None<ThetaFactor>, T1, T2, T3, T4, T5, T6>.div(f: ThetaFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_Temperature")
operator fun <T0: Linear<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: ThetaFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_Temperature")
operator fun
    ThetaProduct<Square<ThetaFactor>, None<ThetaQ1>, None<ThetaQ2>, None<ThetaQ3>, None<ThetaQ4>, None<ThetaQ5>, None<ThetaQ6>>.div(f: ThetaFactor) =
    f.withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Factor_Div_Temperature")
operator fun <T0: Factor<ThetaFactor, Factor<ThetaFactor, TRest>>, TRest: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: ThetaFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_Temperature")
operator fun <T0: Reciproke<ThetaFactor, TRest>, TRest: QuantityExpr<ThetaFactor>, T1: QuantityExpr<ThetaQ1>, T2: QuantityExpr<ThetaQ2>, T3: QuantityExpr<ThetaQ3>, T4: QuantityExpr<ThetaQ4>, T5: QuantityExpr<ThetaQ5>, T6: QuantityExpr<ThetaQ6>>
    ThetaProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: ThetaFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
