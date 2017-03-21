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
	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general/mocked"
)

type ToolkitAdapterMock struct {
	mocked.Base
	LastAction func()
}

func NewToolkitAdapterMock() *ToolkitAdapterMock {
	result := &ToolkitAdapterMock{}
	result.On("Enqueue", mock.Anything).Return()
	result.On("Quit").Return()
	return result
}

func (m *ToolkitAdapterMock) Enqueue(action func()) {
	m.Called(action)
	m.LastAction = action
}

func (m *ToolkitAdapterMock) Quit() {
	m.Called()
}
