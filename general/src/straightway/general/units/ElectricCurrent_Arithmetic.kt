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

private typealias IProduct<I, N, L, J, M, Theta, T> = ProductOfBaseQuantities<N, I, L, J, M, Theta, T>
private fun <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    create(t0: T0, t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) = ProductOfBaseQuantities(t1, t0, t2, t3, t4, t5, t6)

private typealias IFactor = ElectricCurrent
private typealias IQ1 = AmountOfSubstance
private typealias IQ2 = Length
private typealias IQ3 = LuminousIntensity
private typealias IQ4 = Mass
private typealias IQ5 = Temperature
private typealias IQ6 = Time

private val noQ0 = NoI

private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q0 get() = i
private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q1 get() = n
private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q2 get() = l
private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q3 get() = j
private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q4 get() = m
private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q5 get() = theta
private val <T0: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.q6 get() = t

@JvmName("ProductOfBaseQuantities_None_Times_ElectricCurrent")
operator fun <T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<None<IFactor>, T1, T2, T3, T4, T5, T6>.times(f: IFactor) =
    create(linear(f), q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_One_Times_ElectricCurrent")
operator fun
    IProduct<None<IFactor>, None<IQ1>, None<IQ2>, None<IQ3>, None<IQ4>, None<IQ5>, None<IQ6>>.times(f: IFactor) =
    f.timesScaleOf(this)

@JvmName("ProductOfBaseQuantities_Factor_Times_ElectricCurrent")
operator fun <T0: Factor<IFactor, NRest>, NRest: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: IFactor) =
    Factor(f, q0).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_ReciprokeLinear_Times_ElectricCurrent")
operator fun <T0: Reciproke<IFactor, Linear<IFactor>>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: IFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale)

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Times_ElectricCurrent")
operator fun <T0: Reciproke<IFactor, Factor<IFactor, Factor<IFactor, TRest>>>, TRest: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.times(f: IFactor) =
    reciproke(q0.wrapped.rest).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale) }

@JvmName("ProductOfBaseQuantities_One_Div_ElectricCurrent")
operator fun <T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<None<IFactor>, T1, T2, T3, T4, T5, T6>.div(f: IFactor) =
    reciproke(linear(f)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_Linear_Div_ElectricCurrent")
operator fun <T0: Linear<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: IFactor) =
    create(noQ0, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Square_Div_ElectricCurrent")
operator fun
    IProduct<Square<IFactor>, None<IQ1>, None<IQ2>, None<IQ3>, None<IQ4>, None<IQ5>, None<IQ6>>.div(f: IFactor) =
    f.withScale(scale * f.siScale.reciproke)

@JvmName("ProductOfBaseQuantities_Factor_Div_ElectricCurrent")
operator fun <T0: Factor<IFactor, Factor<IFactor, TRest>>, TRest: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: IFactor) =
    q0.rest.let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }

@JvmName("ProductOfBaseQuantities_ReciprokeFactor_Div_ElectricCurrent")
operator fun <T0: Reciproke<IFactor, TRest>, TRest: QuantityExpr<IFactor>, T1: QuantityExpr<IQ1>, T2: QuantityExpr<IQ2>, T3: QuantityExpr<IQ3>, T4: QuantityExpr<IQ4>, T5: QuantityExpr<IQ5>, T6: QuantityExpr<IQ6>>
    IProduct<T0, T1, T2, T3, T4, T5, T6>.div(f: IFactor) =
    reciproke(Factor(f, q0.wrapped)).let { create(it, q1, q2, q3, q4, q5, q6).withScale(scale * f.siScale.reciproke) }
