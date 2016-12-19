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
	"fmt"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/general/mocked"
)

type ConnectorMock struct {
	mocked.Base
	Identifier id.Type
}

var objectCount = 0

func NewConnectorMock() *ConnectorMock {
	peer := &ConnectorMock{}
	peer.Identifier = id.FromString(fmt.Sprintf("%v", objectCount))
	objectCount++
	peer.On("Id").Return()
	peer.On("Equal", mock.Anything).Return()
	peer.On("Push", mock.Anything, mock.Anything).Return()
	peer.On("CloseConnectionWith", mock.Anything).Return()
	peer.On("RequestConnectionWith", mock.Anything).Return()
	peer.On("AnnouncePeersFrom", mock.Anything, mock.Anything).Return()
	peer.On("RequestPeers", mock.Anything).Return()
	peer.On("Query", mock.Anything, mock.Anything).Return()
	return peer
}

func (m *ConnectorMock) Id() id.Type {
	m.Called()
	return m.Identifier
}

func (m *ConnectorMock) Equal(other general.Equaler) bool {
	m.Called(other)
	otherIdentifable, ok := other.(id.Holder)
	return ok && otherIdentifable.Id() == m.Id()
}

func (m *ConnectorMock) Startup() {
	m.Called()
}

func (m *ConnectorMock) RequestConnectionWith(peer Connector) {
	m.Called(peer)
}

func (m *ConnectorMock) RequestPeers(receiver Connector) {
	m.Called(receiver)
}

func (m *ConnectorMock) AnnouncePeersFrom(from Connector, peers []Connector) {
	m.Called(from, peers)
}

func (m *ConnectorMock) CloseConnectionWith(peer Connector) {
	m.Called(peer)
}

func (m *ConnectorMock) Push(data *data.Chunk, origin id.Holder) {
	m.Called(data, origin)
}

func (m *ConnectorMock) Query(query data.Query, receiver PusherWithId) {
	m.Called(query, receiver)
}

func Connectors(cs []*ConnectorMock) []Connector {
	result := make([]Connector, len(cs))
	for i, peer := range cs {
		result[i] = peer
	}
	return result
}

func Pushers(cs []*ConnectorMock) []Pusher {
	result := make([]Pusher, len(cs))
	for i, peer := range cs {
		result[i] = peer
	}
	return result
}

func Queryables(cs []*ConnectorMock) []Queryable {
	result := make([]Queryable, len(cs))
	for i, peer := range cs {
		result[i] = peer
	}
	return result
}
