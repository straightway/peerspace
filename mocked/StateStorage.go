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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
)

type StateStorage struct {
	Base
	KnownPeers []peer.Connector
}

func NewStateStorage(knownPeers ...peer.Connector) *StateStorage {
	result := &StateStorage{KnownPeers: knownPeers}
	result.On("GetAllKnownPeers").Return()
	result.On("IsKnownPeer", mock.Anything).Return()
	result.On("AddKnownPeer", mock.Anything).Return()
	return result
}

func (m *StateStorage) GetAllKnownPeers() []peer.Connector {
	m.Called()
	return m.KnownPeers
}

func (m *StateStorage) IsKnownPeer(peer peer.Connector) bool {
	m.Called(peer)
	return general.Contains(m.KnownPeers, peer)
}

func (m *StateStorage) AddKnownPeer(peer peer.Connector) {
	m.Called(peer)
	m.KnownPeers = append(m.KnownPeers, peer)
}
