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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/general/mocked"
)

type NodeMock struct {
	mocked.Base
	isStarted bool
}

func NewNodeMock(id string) *NodeMock {
	result := &NodeMock{}
	result.On("Id").Return(id)
	result.On("Equal", result).Return(true)
	result.On("Equal", mock.Anything).Return(false)
	result.On("Startup").Return()
	result.On("ShutDown").Return()
	result.On("IsStarted").Return()
	result.On("RequestConnectionWith", mock.Anything).Return()
	result.On("CloseConnectionWith", mock.Anything).Return()
	result.On("AnnouncePeersFrom", mock.Anything, mock.Anything).Return()
	result.On("RequestPeers").Return()
	result.On("Push", mock.Anything, mock.Anything).Return()
	result.On("Query", mock.Anything, mock.Anything).Return()
	return result
}

func (m *NodeMock) Id() string {
	return m.Called().Get(0).(string)
}

func (m *NodeMock) Equal(other general.Equaler) bool {
	return m.Called(other).Get(0).(bool)
}

func (m *NodeMock) Startup() {
	m.Called()
	if m.isStarted {
		panic("NodeMock is already started")
	}
	m.isStarted = true
}

func (m *NodeMock) ShutDown() {
	m.Called()
	if false == m.isStarted {
		panic("NodeMock is not started")
	}
	m.isStarted = false
}

func (m *NodeMock) IsStarted() bool {
	m.Called()
	return m.isStarted
}

func (m *NodeMock) RequestConnectionWith(peer Connector) {
	m.Called(peer)
}

func (m *NodeMock) CloseConnectionWith(peer Connector) {
	m.Called(peer)
}

func (m *NodeMock) AnnouncePeersFrom(from Connector, peers []Connector) {
	m.Called(from, peers)
}

func (m *NodeMock) RequestPeers(receiver Connector) {
	m.Called(receiver)
}

func (m *NodeMock) Push(data *data.Chunk, origin id.Holder) {
	m.Called(data, origin)
}

func (m *NodeMock) Query(query data.Query, receiver PusherWithId) {
	m.Called(query, receiver)
}
