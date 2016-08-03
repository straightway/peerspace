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
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
)

type ConnectionStrategy struct {
	Base
}

func NewConnectionStrategy(connectedPeers []peer.Connector) *ConnectionStrategy {
	cs := &ConnectionStrategy{}
	cs.On("IsConnectionAcceptedWith", mock.Anything).Return(true)
	cs.On("PeersToConnect", mock.Anything).Return(connectedPeers)

	return cs
}

func (m *ConnectionStrategy) IsConnectionAcceptedWith(peer id.Holder) bool {
	args := m.Called(peer)
	return args.Get(0).(bool)
}

func (m *ConnectionStrategy) PeersToConnect(allPeers []peer.Connector) []peer.Connector {
	args := m.Called(allPeers)
	return args.Get(0).([]peer.Connector)
}
