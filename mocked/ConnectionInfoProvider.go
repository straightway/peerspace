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

import "github.com/straightway/straightway/peer"

type ConnectionInfoProvider struct {
	Base
	AllConnectingPeers []peer.Connector
	AllConnectedPeers  []peer.Connector
}

func NewConnectionInfoProvider() *ConnectionInfoProvider {
	result := &ConnectionInfoProvider{}
	result.On("ConnectingPeers").Return()
	result.On("ConnectedPeers").Return()
	return result
}

func (m *ConnectionInfoProvider) ConnectingPeers() []peer.Connector {
	m.Called()
	return m.AllConnectingPeers
}

func (m *ConnectionInfoProvider) ConnectedPeers() []peer.Connector {
	m.Called()
	return m.AllConnectedPeers
}
