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

package sim

import "github.com/straightway/straightway/general/mocked"

type ControllerMock struct {
	mocked.Base
	ExecEventHandlers []func()
}

func NewControllerMock() *ControllerMock {
	result := &ControllerMock{}
	result.On("Run").Return()
	result.On("Stop").Return()
	result.On("Resume").Return()
	result.On("Reset").Return()
	result.On("RegisterForExecEvent").Return()
	return result
}

func (m *ControllerMock) Run() {
	m.Called()
}

func (m *ControllerMock) Stop() {
	m.Called()
}

func (m *ControllerMock) Resume() {
	m.Called()
}

func (m *ControllerMock) Reset() {
	m.Called()
}

func (m *ControllerMock) RegisterForExecEvent(callback func()) {
	m.Called()
	m.ExecEventHandlers = append(m.ExecEventHandlers, callback)
}
