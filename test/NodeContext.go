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
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
)

type NodeContext struct {
	node               peer.Node
	stateStorage       *mocked.StateStorage
	connectionStrategy *mocked.ConnectorSelector
	forwardStrategy    *mocked.ConnectorSelector
	knownPeers         []peer.Connector
	connectedPeers     []*mocked.PeerConnector
	notConnectedPeers  []*mocked.PeerConnector
}

// Construction

func NewNodeContext() *NodeContext {
	newNodeContext := &NodeContext{
		knownPeers:         make([]peer.Connector, 0),
		connectionStrategy: &mocked.ConnectorSelector{},
		forwardStrategy:    &mocked.ConnectorSelector{},
		connectedPeers:     []*mocked.PeerConnector{},
		notConnectedPeers:  []*mocked.PeerConnector{},
	}

	return newNodeContext
}

// Public

func (this *NodeContext) AddKnownUnconnectedPeer(peer *mocked.PeerConnector) {
	if peer == nil {
		peer = &mocked.PeerConnector{}
	}
	this.knownPeers = append(this.knownPeers, peer)
	this.notConnectedPeers = append(this.notConnectedPeers, peer)
}

func (this *NodeContext) AddKnownConnectedPeer(peer *mocked.PeerConnector) {
	if peer == nil {
		peer = &mocked.PeerConnector{}
	}
	this.knownPeers = append(this.knownPeers, peer)
	this.connectedPeers = append(this.connectedPeers, peer)
}

func (this *NodeContext) SetUp() {
	this.setupPeers()
	this.createSut()
}

// Private

func (this *NodeContext) setupPeers() {
	this.stateStorage = &mocked.StateStorage{}
	this.stateStorage.
		On("GetAllKnownPeers").
		Return(this.knownPeers)
	this.connectionStrategy.
		On("SelectedConnectors", this.knownPeers).
		Return(mocked.IPeerConnectors(this.connectedPeers))
}

func (this *NodeContext) createSut() {
	this.node = peer.NewNode(
		this.stateStorage,
		this.forwardStrategy,
		this.connectionStrategy)

	for _, p := range this.connectedPeers {
		p.On("Connect", this.node).Return()
	}
}
