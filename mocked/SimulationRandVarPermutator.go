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

package mocked

import "github.com/stretchr/testify/mock"

type SimulationRandVarPermutator struct {
	Base
}

func NewSimulationRandVarPermutator(permutation ...int) *SimulationRandVarPermutator {
	result := &SimulationRandVarPermutator{}
	if len(permutation) == 0 {
		result.On("Perm", mock.Anything).Return(nil)
	} else {
		result.On("Perm", len(permutation)).Return(permutation)
	}
	return result
}

func (m *SimulationRandVarPermutator) Perm(n int) []int {
	result := m.Called(n).Get(0)

	if result != nil {
		return result.([]int)
	}

	identity := make([]int, n)
	for i := 0; i < n; i++ {
		identity[i] = i
	}

	return identity
}
