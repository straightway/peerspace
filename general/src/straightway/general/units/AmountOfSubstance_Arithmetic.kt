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

private typealias NProduct<N, I, L, J, M, Theta, T> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t0, t1, t2, t3, t4, t5, t6)

private typealias NFactor = AmountOfSubstance
private typealias NQ1 = ElectricCurrent
private typealias NQ2 = Length
private typealias NQ3 = LuminousIntensity
private typealias NQ4 = Mass
private typealias NQ5 = Temperature
private typealias NQ6 = Time

private val noQ0 = NoN

private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = n
private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = i
private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = l
private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = j
private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = m
private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = theta
private val <T0: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = t

@JvmName("ProductOfBaseQuantities_None_Times_AmountOfSubstance")
operator fun <T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<None<NFactor>, T1, T2, T3, T4, T5, T6>.times(f: NFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_AmountOfSubstance")
operator fun
    NProduct<None<NFactor>, None<NQ1>, None<NQ2>, None<NQ3>, None<NQ4>, None<NQ5>, None<NQ6>>.times(f: NFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_AmountOfSubstance")
operator fun <T0: Factor<NFactor, NRest>, NRest: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: NFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_AmountOfSubstance")
operator fun <T0: Reciproke<NFactor, Linear<NFactor>>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: NFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_AmountOfSubstance")
operator fun <T0: Reciproke<NFactor, Factor<NFactor, Factor<NFactor, TRest>>>, TRest: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: NFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_AmountOfSubstance")
operator fun <T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<None<NFactor>, T1, T2, T3, T4, T5, T6>.div(f: NFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_AmountOfSubstance")
operator fun <T0: Linear<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: NFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_AmountOfSubstance")
operator fun
    NProduct<Square<NFactor>, None<NQ1>, None<NQ2>, None<NQ3>, None<NQ4>, None<NQ5>, None<NQ6>>.div(f: NFactor) =
    f.withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Factor_Div_AmountOfSubstance")
operator fun <T0: Factor<NFactor, Factor<NFactor, TRest>>, TRest: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: NFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_AmountOfSubstance")
operator fun <T0: Reciproke<NFactor, TRest>, TRest: QuantityExpr<NFactor>, T1: QuantityExpr<NQ1>, T2: QuantityExpr<NQ2>, T3: QuantityExpr<NQ3>, T4: QuantityExpr<NQ4>, T5: QuantityExpr<NQ5>, T6: QuantityExpr<NQ6>>
    NProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: NFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
