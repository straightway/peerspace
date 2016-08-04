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
	"github.com/straightway/straightway/general/times"
)

type RawStorageWithLifeCycle struct {
	*RawStorage
}

func NewRawStorageWithLifeCycle(timer times.Provider) *RawStorageWithLifeCycle {
	result := &RawStorageWithLifeCycle{}
	result.RawStorage = NewRawStorage(timer)
	result.On("Startup").Return()
	result.On("ShutDown").Return()
	return result
}

func (m *RawStorageWithLifeCycle) Startup() {
	m.Called()
}

func (m *RawStorageWithLifeCycle) ShutDown() {
	m.Called()
}
