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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/general/times"
	"github.com/straightway/straightway/peer"
)

type NodeBase struct {
	Identifier    string
	StateStorage  peer.StateStorage
	Timer         times.Provider
	Configuration *app.Configuration

	connectingPeers []peer.Connector
	connectedPeers  []peer.Connector
	isStarted       bool
}

func (this *NodeBase) Id() string {
	return this.Identifier
}

func (this *NodeBase) Equal(other general.Equaler) bool {
	connector, ok := other.(peer.Connector)
	return ok && connector.Id() == this.Id()
}

func (this *NodeBase) IsStarted() bool {
	return this.isStarted
}

func (this *NodeBase) CloseConnectionWith(peer peer.Connector) {
	this.connectingPeers = removePeer(this.connectingPeers, peer)
	this.connectedPeers = removePeer(this.connectedPeers, peer)
}

func (this *NodeBase) IsConnectionPendingWith(peer peer.Connector) bool {
	return slice.Contains(this.connectingPeers, peer)
}

func (this *NodeBase) IsConnectedWith(peer peer.Connector) bool {
	return slice.Contains(this.connectedPeers, peer)
}

func (this *NodeBase) ConnectedPeers() []peer.Connector {
	return append([]peer.Connector(nil), this.connectedPeers...)
}

func (this *NodeBase) ConnectingPeers() []peer.Connector {
	return append([]peer.Connector(nil), this.connectingPeers...)
}

// Private

func (this *NodeBase) acceptConnectionWith(peer peer.Connector) {
	this.connectedPeers = append(this.connectedPeers, peer)
	this.connectingPeers = removePeer(this.connectingPeers, peer)
}

func removePeer(peers []peer.Connector, peerToRemove peer.Connector) []peer.Connector {
	return slice.RemoveItemsIf(peers, func(p interface{}) bool {
		return peerToRemove.Equal(p.(peer.Connector))
	}).([]peer.Connector)
}

func (this *NodeBase) baseStartup() {
	this.isStarted = true
}

func (this *NodeBase) baseShutDown(myself peer.Connector) {
	defer func() { this.isStarted = false }()
	for _, p := range append(this.connectingPeers, this.connectedPeers...) {
		p.CloseConnectionWith(myself)
	}

	this.connectingPeers = nil
	this.connectedPeers = nil
}
