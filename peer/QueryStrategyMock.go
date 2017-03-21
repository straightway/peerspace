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

package peer

import (
	"time"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/duration"
	"github.com/straightway/straightway/general/mocked"
)

type QueryStrategyMock struct {
	mocked.Base
}

func NewQueryStrategyMock(resultPeers []Queryable) *QueryStrategyMock {
	result := &QueryStrategyMock{}
	result.On("ForwardTargetsFor", mock.Anything, mock.Anything).Return(resultPeers)
	result.On("TimeoutFor", mock.Anything).Return(duration.Parse("2h"))
	result.On("IsQueryAccepted", mock.Anything, mock.Anything).Return(true)
	return result
}

func (m *QueryStrategyMock) IsQueryAccepted(query data.Query, receiver Pusher) bool {
	args := m.Called(query, receiver)
	return args.Get(0).(bool)
}

func (m *QueryStrategyMock) ForwardTargetsFor(query data.Query, receiver PusherWithId) []Queryable {
	args := m.Called(receiver, query)
	return args.Get(0).([]Queryable)
}

func (m *QueryStrategyMock) TimeoutFor(query data.Query) time.Duration {
	args := m.Called(query)
	return args.Get(0).(time.Duration)
}
