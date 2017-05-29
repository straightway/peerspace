/**************************************
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
 **************************************/
package straightway.general.units

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PrimitiveArithmeticTest {

    @Test fun mol_Times_mol() =
        assertEquals(ProductOfBaseQuantities(square(mol), NoI, NoL, NoJ, NoM, NoTheta, NoT), mol * mol)
    @Test fun mol_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(linear(mol), linear(ampere), NoL, NoJ, NoM, NoTheta, NoT), mol * ampere)
    @Test fun mol_Times_meter() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, linear(meter), NoJ, NoM, NoTheta, NoT), mol * meter)
    @Test fun mol_Times_candela() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, linear(candela), NoM, NoTheta, NoT), mol * candela)
    @Test fun mol_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, linear(kilo(gramm)), NoTheta, NoT), mol * kilo(gramm))
    @Test fun mol_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, NoM, linear(kelvin), NoT), mol * kelvin)
    @Test fun mol_Times_second() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, NoM, NoTheta, linear(second)), mol * second)

    @Test fun ampere_Times_mol() =
        assertEquals(ProductOfBaseQuantities(linear(mol), linear(ampere), NoL, NoJ, NoM, NoTheta, NoT), ampere * mol)
    @Test fun ampere_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(NoN, square(ampere), NoL, NoJ, NoM, NoTheta, NoT), ampere * ampere)
    @Test fun ampere_Times_meter() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), linear(meter), NoJ, NoM, NoTheta, NoT), ampere * meter)
    @Test fun ampere_Times_candela() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, linear(candela), NoM, NoTheta, NoT), ampere * candela)
    @Test fun ampere_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, linear(kilo(gramm)), NoTheta, NoT), ampere * kilo(gramm))
    @Test fun ampere_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, NoM, linear(kelvin), NoT), ampere * kelvin)
    @Test fun ampere_Times_second() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, NoM, NoTheta, linear(second)), ampere * second)

    @Test fun meter_Times_mol() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, linear(meter), NoJ, NoM, NoTheta, NoT), meter * mol)
    @Test fun meter_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), linear(meter), NoJ, NoM, NoTheta, NoT), meter * ampere)
    @Test fun meter_Times_meter() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, square(meter), NoJ, NoM, NoTheta, NoT), meter * meter)
    @Test fun meter_Times_candela() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), linear(candela), NoM, NoTheta, NoT), meter * candela)
    @Test fun meter_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, linear(kilo(gramm)), NoTheta, NoT), meter * kilo(gramm))
    @Test fun meter_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, NoM, linear(kelvin), NoT), meter * kelvin)
    @Test fun meter_Times_second() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, NoM, NoTheta, linear(second)), meter * second)

    @Test fun candela_Times_mol() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, linear(candela), NoM, NoTheta, NoT), candela * mol)
    @Test fun candela_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, linear(candela), NoM, NoTheta, NoT), candela * ampere)
    @Test fun candela_Times_meter() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), linear(candela), NoM, NoTheta, NoT), candela * meter)
    @Test fun candela_Times_candela() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, square(candela), NoM, NoTheta, NoT), candela * candela)
    @Test fun candela_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), linear(kilo(gramm)), NoTheta, NoT), candela * kilo(gramm))
    @Test fun candela_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), NoM, linear(kelvin), NoT), candela * kelvin)
    @Test fun candela_Times_second() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), NoM, NoTheta, linear(second)), candela * second)

    @Test fun kilogramm_Times_mol() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, linear(kilo(gramm)), NoTheta, NoT), kilo(gramm) * mol)
    @Test fun kilogramm_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, linear(kilo(gramm)), NoTheta, NoT), kilo(gramm) * ampere)
    @Test fun kilogramm_Times_meter() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, linear(kilo(gramm)), NoTheta, NoT), kilo(gramm) * meter)
    @Test fun kilogramm_Times_candela() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), linear(kilo(gramm)), NoTheta, NoT), kilo(gramm) * candela)
    @Test fun kilogramm_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, square(kilo(gramm)), NoTheta, NoT), kilo(gramm) * kilo(gramm))
    @Test fun kilogramm_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, linear(kilo(gramm)), linear(kelvin), NoT), kilo(gramm) * kelvin)
    @Test fun kilogramm_Times_second() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, linear(kilo(gramm)), NoTheta, linear(second)), kilo(gramm) * second)

    @Test fun kelvin_Times_mol() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, NoM, linear(kelvin), NoT), kelvin * mol)
    @Test fun kelvin_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, NoM, linear(kelvin), NoT), kelvin * ampere)
    @Test fun kelvin_Times_meter() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, NoM, linear(kelvin), NoT), kelvin * meter)
    @Test fun kelvin_Times_candela() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), NoM, linear(kelvin), NoT), kelvin * candela)
    @Test fun kelvin_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, linear(kilo(gramm)), linear(kelvin), NoT), kelvin * kilo(gramm))
    @Test fun kelvin_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, square(kelvin), NoT), kelvin * kelvin)
    @Test fun kelvin_Times_second() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, linear(kelvin), linear(second)), kelvin * second)

    @Test fun second_Times_mol() =
        assertEquals(ProductOfBaseQuantities(linear(mol), NoI, NoL, NoJ, NoM, NoTheta, linear(second)), second * mol)
    @Test fun second_Times_ampere() =
        assertEquals(ProductOfBaseQuantities(NoN, linear(ampere), NoL, NoJ, NoM, NoTheta, linear(second)), second * ampere)
    @Test fun second_Times_meter() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, linear(meter), NoJ, NoM, NoTheta, linear(second)), second * meter)
    @Test fun second_Times_candela() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, linear(candela), NoM, NoTheta, linear(second)), second * candela)
    @Test fun second_Times_kilogramm() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, linear(kilo(gramm)), NoTheta, linear(second)), second * kilo(gramm))
    @Test fun second_Times_kelvin() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, linear(kelvin), linear(second)), second * kelvin)
    @Test fun second_Times_second() =
        assertEquals(ProductOfBaseQuantities(NoN, NoI, NoL, NoJ, NoM, NoTheta, square(second)), second * second)
}