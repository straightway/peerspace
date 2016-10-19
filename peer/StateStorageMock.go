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

	"github.com/straightway/straightway/general/mocked"
	"github.com/straightway/straightway/general/slice"
)

type StateStorageMock struct {
	mocked.Base
	KnownPeers []Connector
}

func NewStateStorageMock(knownPeers ...Connector) *StateStorageMock {
	result := &StateStorageMock{KnownPeers: knownPeers}
	result.On("GetAllKnownPeers").Return()
	result.On("IsKnownPeer", mock.Anything).Return()
	result.On("AddKnownPeer", mock.Anything).Return()
	return result
}

func (m *StateStorageMock) GetAllKnownPeers() []Connector {
	m.Called()
	return m.KnownPeers
}

func (m *StateStorageMock) IsKnownPeer(peer Connector) bool {
	m.Called(peer)
	return slice.Contains(m.KnownPeers, peer)
}

func (m *StateStorageMock) AddKnownPeer(peer Connector) {
	m.Called(peer)
	m.KnownPeers = append(m.KnownPeers, peer)
}
