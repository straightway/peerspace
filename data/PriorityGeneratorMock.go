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

package data

import (
	"time"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general/mocked"
)

type PriorityGeneratorMock struct {
	mocked.Base
}

func NewPriorityGeneratorMock(priority float32, expirationTime time.Time) *PriorityGeneratorMock {
	result := &PriorityGeneratorMock{}
	result.On("Priority", mock.Anything).Return(priority, expirationTime)
	return result
}

func (m *PriorityGeneratorMock) Priority(chunk *Chunk) (float32, time.Time) {
	args := m.Called(chunk)
	return args.Get(0).(float32), args.Get(1).(time.Time)
}
