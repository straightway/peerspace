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
	"time"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
)

type DoForward bool

type NodeContextBase struct {
	node              peer.Node
	stateStorage      *mocked.StateStorage
	knownPeers        []peer.Connector
	connectedPeers    []*mocked.PeerConnector
	notConnectedPeers []*mocked.PeerConnector
	forwardPeers      []*mocked.PeerConnector
	timer             *mocked.Timer
	configuration     *app.Configuration
	createSut         func() peer.Node
	setupPeers        func()
}

func (this *NodeContextBase) AddKnownUnconnectedPeer() *mocked.PeerConnector {
	peer := mocked.NewPeerConnector()
	this.knownPeers = append(this.knownPeers, peer)
	this.notConnectedPeers = append(this.notConnectedPeers, peer)
	this.SetUp()
	return peer
}

func (this *NodeContextBase) AddKnownConnectedPeer(forward DoForward) *mocked.PeerConnector {
	peer := mocked.NewPeerConnector()
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

func (this *NodeContextBase) ShutDownNode() {
	if this.node != nil {
		this.node.ShutDown()
	}
}

func (this *NodeContextBase) ConfirmConnectedPeers() {
	for _, p := range this.connectedPeers {
		this.node.RequestConnectionWith(p)
	}
}

func (this *NodeContextBase) AdvanceTimeBy(span time.Duration) {
	this.timer.CurrentTime = this.timer.CurrentTime.Add(span)
}

func (this *NodeContextBase) SetUp() {
	isNodeStarted := this.node != nil && this.node.IsStarted()
	if isNodeStarted {
		this.node.ShutDown()
	}
	this.stateStorage = mocked.NewStateStorage(this.knownPeers...)
	this.setupPeers()
	this.node = this.createSut()
	if isNodeStarted {
		this.node.Startup()
	}
}
