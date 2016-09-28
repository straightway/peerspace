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

import (
	"fmt"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/simc/ui"
	"github.com/stretchr/testify/mock"
)

type SimulationUiNodeModel struct {
	Base
	connections []ui.NodeModel
	x, y        float64
}

func NewSimulationUiNodeModel(id string, x, y float64) *SimulationUiNodeModel {
	result := &SimulationUiNodeModel{x: x, y: y}
	result.On("Position").Return()
	result.On("SetPosition", mock.Anything, mock.Anything).Return()
	result.On("Connections").Return()
	result.On("Id").Return(id)
	result.On("Equal").Return(false)
	return result
}

func (m *SimulationUiNodeModel) Id() string {
	return m.Called().Get(0).(string)
}

func (m *SimulationUiNodeModel) Equal(other general.Equaler) bool {
	return m.Called().Get(0).(bool)
}

func (m *SimulationUiNodeModel) Position() (x, y float64) {
	m.Called()
	return m.x, m.y
}

func (m *SimulationUiNodeModel) SetPosition(x, y float64) {
	m.Called(x, y)
	m.x, m.y = x, y
}

func (m *SimulationUiNodeModel) Connections() []ui.NodeModel {
	m.Called()
	return m.connections
}

func (m *SimulationUiNodeModel) ConnectTo(other *SimulationUiNodeModel) {
	m.connections = append(m.connections, other)
}

func (m *SimulationUiNodeModel) String() string {
	return fmt.Sprintf("(%v, %v)", m.x, m.y)
}
