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

package test

import (
	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/peerc"
)

type NodeContext struct {
	NodeContextBase
	node                 *peerc.Node
	dataStorage          *mocked.DataStorage
	announcementStrategy *mocked.AnnouncementStrategy
	connectionStrategy   *mocked.ConnectionStrategy
	dataStrategy         *mocked.DataStrategy
	queryStrategy        *mocked.QueryStrategy
}

// Construction

func NewNodeContext() *NodeContext {
	newNodeContext := &NodeContext{
		connectionStrategy:   mocked.NewConnectionStrategy(nil),
		announcementStrategy: mocked.NewAnnouncementStrategy(),
		dataStrategy:         &mocked.DataStrategy{},
		queryStrategy:        &mocked.QueryStrategy{}}
	newNodeContext.timer = &mocked.Timer{}
	newNodeContext.configuration = &app.Configuration{}
	newNodeContext.createSut = newNodeContext.createNode
	newNodeContext.NodeContextBase.setupPeers = newNodeContext.setupPeers

	return newNodeContext
}

// Public

func (this *NodeContext) SetDataStorage(newDataStorage *mocked.DataStorage) {
	this.dataStorage = newDataStorage
	this.SetUp()
}

func (this *NodeContext) Push(chunk *data.Chunk) {
	this.node.Push(chunk, this.FirstConnectedPeer())
}

func (this *NodeContext) FirstConnectedPeer() peer.Connector {
	return this.node.ConnectedPeers()[0]
}

// Private

func (this *NodeContext) setupPeers() {
	this.connectionStrategy = mocked.NewConnectionStrategy(mocked.IPeerConnectors(this.connectedPeers))
	this.dataStrategy = mocked.NewDataStrategy(mocked.IPushers(this.forwardPeers))
	this.queryStrategy = mocked.NewQueryForwardStrategy(mocked.IQueryables(this.forwardPeers))
}

func (this *NodeContext) createNode() peer.Node {
	if this.dataStorage == nil {
		this.dataStorage = mocked.NewDataStorage(nil)
	}
	this.node = &peerc.Node{}
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
