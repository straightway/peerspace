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
	"testing"
	"time"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/peerc"
	"github.com/stretchr/testify/mock"
)

type DoForward bool

type NodeContext struct {
	node                 *peerc.Node
	dataStorage          *mocked.DataStorage
	stateStorage         *mocked.StateStorage
	announcementStrategy *mocked.AnnouncementStrategy
	connectionStrategy   *mocked.ConnectionStrategy
	dataStrategy         *mocked.DataStrategy
	queryStrategy        *mocked.QueryStrategy
	knownPeers           []peer.Connector
	connectedPeers       []*mocked.PeerConnector
	notConnectedPeers    []*mocked.PeerConnector
	forwardPeers         []*mocked.PeerConnector
	timer                *mocked.Timer
	configuration        *app.Configuration
}

// Construction

func NewNodeContext() *NodeContext {
	newNodeContext := &NodeContext{
		knownPeers:           make([]peer.Connector, 0),
		connectionStrategy:   mocked.NewConnectionStrategy(nil),
		announcementStrategy: mocked.NewAnnouncementStrategy(),
		dataStrategy:         &mocked.DataStrategy{},
		queryStrategy:        &mocked.QueryStrategy{},
		connectedPeers:       []*mocked.PeerConnector{},
		notConnectedPeers:    []*mocked.PeerConnector{},
		timer:                &mocked.Timer{},
		configuration:        &app.Configuration{}}

	return newNodeContext
}

// Public

func (this *NodeContext) AddKnownUnconnectedPeer() *mocked.PeerConnector {
	peer := &mocked.PeerConnector{}
	this.knownPeers = append(this.knownPeers, peer)
	this.notConnectedPeers = append(this.notConnectedPeers, peer)
	this.SetUp()
	return peer
}

func (this *NodeContext) AddKnownConnectedPeer(forward DoForward) *mocked.PeerConnector {
	peer := mocked.CreatePeerConnector()
	this.knownPeers = append(this.knownPeers, peer)
	this.connectedPeers = append(this.connectedPeers, peer)
	if bool(forward) {
		this.forwardPeers = append(this.forwardPeers, peer)
	}
	this.SetUp()
	if this.node.IsStarted() {
		this.ConfirmConnectedPeers()
	}
	return peer
}

func (this *NodeContext) ConfirmConnectedPeers() {
	for _, p := range this.connectedPeers {
		this.node.RequestConnectionWith(p)
	}
}

func (this *NodeContext) SetDataStorage(newDataStorage *mocked.DataStorage) {
	this.dataStorage = newDataStorage
	this.SetUp()
}

func (this *NodeContext) SetUp() {
	isNodeStarted := this.node != nil && this.node.IsStarted()
	if isNodeStarted {
		this.node.ShutDown()
	}
	this.setupPeers()
	this.createSut()
	if isNodeStarted {
		this.node.Startup()
	}
}

func (this *NodeContext) ShutDownNode() {
	if this.node != nil {
		this.node.ShutDown()
	}
}

func (this *NodeContext) AdvanceTimeBy(span time.Duration) {
	this.timer.CurrentTime = this.timer.CurrentTime.Add(span)
}

func (this *NodeContext) FirstConnectedPeer() peer.Connector {
	return this.node.ConnectedPeers()[0]
}

func (this *NodeContext) Push(chunk *data.Chunk) {
	this.node.Push(chunk, this.FirstConnectedPeer())
}

func AssertPushed(t *testing.T, receiver *mocked.PeerConnector, chunks ...*data.Chunk) {
	for _, chunk := range chunks {
		receiver.AssertCalled(t, "Push", chunk, mock.Anything)
	}
	receiver.AssertNumberOfCalls(t, "Push", len(chunks))
}

// Private

func (this *NodeContext) setupPeers() {
	this.stateStorage = mocked.NewStateStorage(this.knownPeers...)
	this.connectionStrategy = mocked.NewConnectionStrategy(mocked.IPeerConnectors(this.connectedPeers))
	this.dataStrategy = mocked.NewDataStrategy(mocked.IPeerConnectors(this.forwardPeers))
	this.queryStrategy = mocked.NewQueryForwardStrategy(mocked.IPeerConnectors(this.forwardPeers))
}

func (this *NodeContext) createSut() {
	if this.dataStorage == nil {
		this.dataStorage = mocked.NewDataStorage(nil)
	}
	this.node = &peerc.Node{
		StateStorage:         this.stateStorage,
		DataStorage:          this.dataStorage,
		AnnouncementStrategy: this.announcementStrategy,
		DataStrategy:         this.dataStrategy,
		QueryStrategy:        this.queryStrategy,
		ConnectionStrategy:   this.connectionStrategy,
		Timer:                this.timer,
		Configuration:        this.configuration}

	for _, p := range this.connectedPeers {
		p.On("RequestConnectionWith", this.node).Return()
	}
}
