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
	"fmt"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
)

type PeerConnector struct {
	Base
	Identifier string
}

var objectCount = 0

func CreatePeerConnector() *PeerConnector {
	peer := &PeerConnector{}
	peer.Identifier = fmt.Sprintf("%v", objectCount)
	objectCount++
	peer.On("Push", mock.Anything, mock.Anything)
	peer.On("CloseConnectionWith", mock.Anything)
	peer.On("RequestConnectionWith", mock.Anything)
	peer.On("AnnouncePeers", mock.Anything)
	peer.On("RequestPeers", mock.Anything)
	peer.On("Query", mock.Anything, mock.Anything)
	return peer
}

func (this *PeerConnector) Id() string {
	return this.Identifier
}

func (this *PeerConnector) Equal(other general.Equaler) bool {
	otherIdentifable, ok := other.(id.Holder)
	return ok && otherIdentifable.Id() == this.Id()
}

func (m *PeerConnector) Startup() {
	m.Called()
}

func (m *PeerConnector) RequestConnectionWith(peer peer.Connector) {
	m.Called(peer)
}

func (m *PeerConnector) RequestPeers(receiver peer.Connector) {
	m.Called(receiver)
}

func (m *PeerConnector) AnnouncePeers(peers []peer.Connector) {
	m.Called(peers)
}

func (m *PeerConnector) CloseConnectionWith(peer peer.Connector) {
	m.Called(peer)
}

func (m *PeerConnector) Push(data *data.Chunk, origin id.Holder) {
	m.Called(data, origin)
}

func (m *PeerConnector) Query(query data.Query, receiver peer.PusherWithId) {
	m.Called(query, receiver)
}

func IPeerConnectors(cs []*PeerConnector) []peer.Connector {
	result := make([]peer.Connector, len(cs))
	for i, peer := range cs {
		result[i] = peer
	}
	return result
}

func IPushers(cs []*PeerConnector) []peer.Pusher {
	result := make([]peer.Pusher, len(cs))
	for i, peer := range cs {
		result[i] = peer
	}
	return result
}

func IQueryables(cs []*PeerConnector) []peer.Queryable {
	result := make([]peer.Queryable, len(cs))
	for i, peer := range cs {
		result[i] = peer
	}
	return result
}
