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
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
)

type DataStrategy struct {
	Base
}

func NewDataStrategy(resultPeers []peer.Connector) *DataStrategy {
	result := &DataStrategy{}
	result.On("ForwardTargetsFor", mock.Anything, mock.Anything).Return(resultPeers)
	result.On("IsChunkAccepted", mock.Anything, mock.Anything).Return(true)
	return result
}

func (m *DataStrategy) IsChunkAccepted(chunk *data.Chunk, origin general.Identifyable) bool {
	args := m.Called(chunk, origin)
	return args.Get(0).(bool)
}

func (m *DataStrategy) ForwardTargetsFor(key data.Key, origin general.Identifyable) []peer.Connector {
	args := m.Called(key, origin)
	return args.Get(0).([]peer.Connector)
}
