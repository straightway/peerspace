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
	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/general/mocked"
)

type DataStrategyMock struct {
	mocked.Base
}

func NewDataStrategyMock(resultPeers []Pusher) *DataStrategyMock {
	result := &DataStrategyMock{}
	result.On("ForwardTargetsFor", mock.Anything, mock.Anything).Return(resultPeers)
	result.On("IsChunkAccepted", mock.Anything, mock.Anything).Return(true)
	return result
}

func (m *DataStrategyMock) IsChunkAccepted(chunk *data.Chunk, origin id.Holder) bool {
	args := m.Called(chunk, origin)
	return args.Get(0).(bool)
}

func (m *DataStrategyMock) ForwardTargetsFor(key data.Key, origin id.Holder) []Pusher {
	args := m.Called(key, origin)
	return args.Get(0).([]Pusher)
}
