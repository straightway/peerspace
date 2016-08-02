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

package test

// TODO This is a mock class and should be moved to the mocked package
// However, it is due to cyclic dependencies not possible now.
// Further refactoring is requied.

import (
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simulation"
)

type SimulationAudienceProvider struct {
	mocked.Base
}

func NewSimulationAudienceProvider(audience ...simulation.DataConsumer) *SimulationAudienceProvider {
	result := &SimulationAudienceProvider{}
	result.On("Audience").Return(audience)
	return result
}

func (m *SimulationAudienceProvider) Audience() []simulation.DataConsumer {
	return m.Called().Get(0).([]simulation.DataConsumer)
}
