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
	"time"

	"github.com/stretchr/testify/mock"
)

type SimulationScheduler struct {
	Base
	CurrentTime time.Time
}

func NewSimulationScheduler() *SimulationScheduler {
	result := &SimulationScheduler{}
	result.On("Schedule", mock.Anything, mock.Anything).Return()
	result.On("ScheduleAbsolute", mock.Anything, mock.Anything).Return()
	result.On("Time").Return()
	return result
}

func (m *SimulationScheduler) Schedule(duration time.Duration, action func()) {
	m.Called(duration, action)
}

func (m *SimulationScheduler) ScheduleAbsolute(time time.Time, action func()) {
	m.Called(time, action)
}

func (m *SimulationScheduler) Time() time.Time {
	m.Called()
	return m.CurrentTime
}
