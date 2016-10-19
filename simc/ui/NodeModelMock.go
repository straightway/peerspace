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

package ui

import (
	"fmt"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/mocked"
)

type NodeModelMock struct {
	mocked.Base
	connections []NodeModel
	x, y        float64
}

func NewNodeModelMock(id string, x, y float64) *NodeModelMock {
	result := &NodeModelMock{x: x, y: y}
	result.On("Position").Return()
	result.On("SetPosition", mock.Anything, mock.Anything).Return()
	result.On("Connections").Return()
	result.On("Id").Return(id)
	result.On("Equal").Return(false)
	return result
}

func (m *NodeModelMock) Id() string {
	return m.Called().Get(0).(string)
}

func (m *NodeModelMock) Equal(other general.Equaler) bool {
	return m.Called().Get(0).(bool)
}

func (m *NodeModelMock) Position() (x, y float64) {
	m.Called()
	return m.x, m.y
}

func (m *NodeModelMock) SetPosition(x, y float64) {
	m.Called(x, y)
	m.x, m.y = x, y
}

func (m *NodeModelMock) Connections() []NodeModel {
	m.Called()
	return m.connections
}

func (m *NodeModelMock) ConnectTo(other *NodeModelMock) {
	m.connections = append(m.connections, other)
}

func (m *NodeModelMock) String() string {
	return fmt.Sprintf("(%v, %v)", m.x, m.y)
}
