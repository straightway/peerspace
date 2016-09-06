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

type SimulationControlUi struct {
	Base
}

func NewSimulationControlUi() *SimulationControlUi {
	result := &SimulationControlUi{}
	result.On("SetStartEnabled", mock.Anything).Return()
	result.On("SetStopEnabled", mock.Anything).Return()
	result.On("SetPauseEnabled", mock.Anything).Return()
	return result
}

func (m *SimulationControlUi) SetStartEnabled(enabled bool) {
	m.Called(enabled)
}

func (m *SimulationControlUi) SetStopEnabled(enabled bool) {
	m.Called(enabled)
}

func (m *SimulationControlUi) SetPauseEnabled(enabled bool) {
	m.Called(enabled)
}
