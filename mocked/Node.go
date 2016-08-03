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
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
	"github.com/stretchr/testify/mock"
)

type Node struct {
	Base
	isStarted bool
}

func NewNode(id string) *Node {
	result := &Node{}
	result.On("Id").Return(id)
	result.On("Equal", result).Return(true)
	result.On("Equal", mock.Anything).Return(false)
	result.On("Startup").Return()
	result.On("ShutDown").Return()
	result.On("RequestConnectionWith", mock.Anything).Return()
	result.On("CloseConnectionWith", mock.Anything).Return()
	result.On("AnnouncePeers", mock.Anything).Return()
	result.On("RequestPeers").Return()
	result.On("Push", mock.Anything, mock.Anything).Return()
	result.On("Query", mock.Anything, mock.Anything).Return()
	return result
}

func (m *Node) Id() string {
	return m.Called().Get(0).(string)
}

func (m *Node) Equal(other general.Equaler) bool {
	return m.Called(other).Get(0).(bool)
}

func (m *Node) Startup() {
	m.Called()
	if m.isStarted {
		panic("Node is already started")
	}
	m.isStarted = true
}

func (m *Node) ShutDown() {
	m.Called()
	if false == m.isStarted {
		panic("Node is not started")
	}
	m.isStarted = false
}

func (m *Node) RequestConnectionWith(peer peer.Connector) {
	m.Called(peer)
}

func (m *Node) CloseConnectionWith(peer peer.Connector) {
	m.Called(peer)
}

func (m *Node) AnnouncePeers(peers []peer.Connector) {
	m.Called(peers)
}

func (m *Node) RequestPeers(receiver peer.Connector) {
	m.Called(receiver)
}

func (m *Node) Push(data *data.Chunk, origin id.Holder) {
	m.Called(data, origin)
}

func (m *Node) Query(query data.Query, receiver peer.Pusher) {
	m.Called(query, receiver)
}
