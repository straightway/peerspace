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

import "github.com/straightway/straightway/simc/ui"

type SimulationUiNetworkModel struct {
	Base
}

func NewSimulationUiNetworkModel(nodes ...ui.NodeModel) *SimulationUiNetworkModel {
	result := &SimulationUiNetworkModel{}
	result.On("Nodes").Return(nodes)
	return result
}

func (m *SimulationUiNetworkModel) Nodes() []ui.NodeModel {
	return m.Called().Get(0).([]ui.NodeModel)
}
