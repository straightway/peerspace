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

package peerc

import (
	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/times"
	"github.com/straightway/straightway/peer"
)

type TestNodeContext struct {
	TestNodeContextBase
	node                 *Node
	dataStorage          *data.StorageMock
	announcementStrategy *peer.AnnouncementStrategyMock
	connectionStrategy   *peer.ConnectionStrategyMock
	dataStrategy         *peer.DataStrategyMock
	queryStrategy        *peer.QueryStrategyMock
}

// Construction

func NewNodeContext() *TestNodeContext {
	newNodeContext := &TestNodeContext{
		connectionStrategy:   peer.NewConnectionStrategyMock(nil),
		announcementStrategy: peer.NewAnnouncementStrategyMock(),
		dataStrategy:         &peer.DataStrategyMock{},
		queryStrategy:        &peer.QueryStrategyMock{}}
	newNodeContext.timer = &times.ProviderMock{}
	newNodeContext.configuration = &app.Configuration{}
	newNodeContext.createSut = newNodeContext.createNode
	newNodeContext.TestNodeContextBase.setupPeers = newNodeContext.setupPeers

	return newNodeContext
}

// Public

func (this *TestNodeContext) SetDataStorage(newDataStorage *data.StorageMock) {
	this.dataStorage = newDataStorage
	this.SetUp()
}

func (this *TestNodeContext) Push(chunk *data.Chunk) {
	this.node.Push(chunk, this.FirstConnectedPeer())
}

func (this *TestNodeContext) FirstConnectedPeer() peer.Connector {
	return this.node.ConnectedPeers()[0]
}

// Private

func (this *TestNodeContext) setupPeers() {
	this.connectionStrategy = peer.NewConnectionStrategyMock(peer.Connectors(this.connectedPeers))
	this.dataStrategy = peer.NewDataStrategyMock(peer.Pushers(this.forwardPeers))
	this.queryStrategy = peer.NewQueryStrategyMock(peer.Queryables(this.forwardPeers))
}

func (this *TestNodeContext) createNode() peer.Node {
	if this.dataStorage == nil {
		this.dataStorage = data.NewStorageMock(nil)
	}
	this.node = &Node{}
	this.node.StateStorage = this.stateStorage
	this.node.DataStorage = this.dataStorage
	this.node.AnnouncementStrategy = this.announcementStrategy
	this.node.DataStrategy = this.dataStrategy
	this.node.QueryStrategy = this.queryStrategy
	this.node.ConnectionStrategy = this.connectionStrategy
	this.node.Timer = this.timer
	this.node.Configuration = this.configuration

	for _, p := range this.connectedPeers {
		p.On("RequestConnectionWith", this.node).Return()
	}

	return this.node
}
