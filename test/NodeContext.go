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
	"github.com/stretchr/testify/mock"
)

type DoForward bool

type NodeContext struct {
	node               *peer.NodeImpl
	dataStorage        *mocked.DataStorage
	stateStorage       *mocked.StateStorage
	connectionStrategy *mocked.ConnectionStrategy
	forwardStrategy    *mocked.ConnectorSelector
	knownPeers         []peer.Connector
	connectedPeers     []*mocked.PeerConnector
	notConnectedPeers  []*mocked.PeerConnector
	forwardPeers       []*mocked.PeerConnector
}

// Construction

func NewNodeContext() *NodeContext {
	newNodeContext := &NodeContext{
		knownPeers:         make([]peer.Connector, 0),
		connectionStrategy: mocked.NewConnectionStrategy(),
		forwardStrategy:    &mocked.ConnectorSelector{},
		connectedPeers:     []*mocked.PeerConnector{},
		notConnectedPeers:  []*mocked.PeerConnector{},
	}

	return newNodeContext
}

// Public

func (this *NodeContext) AddKnownUnconnectedPeer() {
	peer := &mocked.PeerConnector{}
	this.knownPeers = append(this.knownPeers, peer)
	this.notConnectedPeers = append(this.notConnectedPeers, peer)
	this.SetUp()
}

func (this *NodeContext) AddKnownConnectedPeer(forward DoForward) {
	peer := mocked.CreatePeerConnector()
	this.knownPeers = append(this.knownPeers, peer)
	this.connectedPeers = append(this.connectedPeers, peer)
	if bool(forward) {
		this.forwardPeers = append(this.forwardPeers, peer)
	}
	this.SetUp()
}

func (this *NodeContext) SetUp() {
	this.setupPeers()
	this.createSut()
}

func (this *NodeContext) ShutDownNode() {
	if this.node != nil {
		this.node.ShutDown()
	}
}

// Private

func (this *NodeContext) setupPeers() {
	this.stateStorage = &mocked.StateStorage{}
	this.stateStorage.
		On("GetAllKnownPeers").
		Return(this.knownPeers)
	this.connectionStrategy = mocked.NewConnectionStrategy()
	this.connectionStrategy.
		On("SelectedConnectors", mock.AnythingOfTypeArgument("[]peer.Connector")).
		Return(mocked.IPeerConnectors(this.connectedPeers))
	this.forwardStrategy = &mocked.ConnectorSelector{}
	this.forwardStrategy.
		On("SelectedConnectors", mock.AnythingOfTypeArgument("[]peer.Connector")).
		Return(mocked.IPeerConnectors(this.forwardPeers))
}

func (this *NodeContext) createSut() {
	if this.dataStorage == nil {
		this.dataStorage = mocked.NewDataStorage(mock.Anything, nil)
	}
	this.node = &peer.NodeImpl{
		StateStorage:       this.stateStorage,
		DataStorage:        this.dataStorage,
		ForwardStrategy:    this.forwardStrategy,
		ConnectionStrategy: this.connectionStrategy}

	for _, p := range this.connectedPeers {
		p.On("RequestConnectionWith", this.node).Return()
	}
}
