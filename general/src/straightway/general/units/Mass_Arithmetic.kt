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

private typealias MProduct<M, I, L, J, N, Theta, T> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t4, t1, t2, t3, t0, t5, t6)

private typealias MFactor = Mass
private typealias MQ1 = ElectricCurrent
private typealias MQ2 = Length
private typealias MQ3 = LuminousIntensity
private typealias MQ4 = AmountOfSubstance
private typealias MQ5 = Temperature
private typealias MQ6 = Time

private val noQ0 = NoM

private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = m
private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = i
private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = l
private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = j
private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = n
private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = theta
private val <T0: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = t

@JvmName("ProductOfBaseQuantities_None_Times_Mass")
operator fun <T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<None<MFactor>, T1, T2, T3, T4, T5, T6>.times(f: MFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_Mass")
operator fun
    MProduct<None<MFactor>, None<MQ1>, None<MQ2>, None<MQ3>, None<MQ4>, None<MQ5>, None<MQ6>>.times(f: MFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_Mass")
operator fun <T0: Factor<MFactor, NRest>, NRest: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: MFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_Mass")
operator fun <T0: Reciproke<MFactor, Linear<MFactor>>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: MFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_Mass")
operator fun <T0: Reciproke<MFactor, Factor<MFactor, Factor<MFactor, TRest>>>, TRest: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: MFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_Mass")
operator fun <T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<None<MFactor>, T1, T2, T3, T4, T5, T6>.div(f: MFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_Mass")
operator fun <T0: Linear<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: MFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_Mass")
operator fun
    MProduct<Square<MFactor>, None<MQ1>, None<MQ2>, None<MQ3>, None<MQ4>, None<MQ5>, None<MQ6>>.div(f: MFactor) =
    f.withScale(siScale * f.siScale.reciproke * f.siScaleCorrection)

@JvmName("ProductOfBaseQuantities_Factor_Div_Mass")
operator fun <T0: Factor<MFactor, Factor<MFactor, TRest>>, TRest: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: MFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_Mass")
operator fun <T0: Reciproke<MFactor, TRest>, TRest: QuantityExpr<MFactor>, T1: QuantityExpr<MQ1>, T2: QuantityExpr<MQ2>, T3: QuantityExpr<MQ3>, T4: QuantityExpr<MQ4>, T5: QuantityExpr<MQ5>, T6: QuantityExpr<MQ6>>
    MProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: MFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
