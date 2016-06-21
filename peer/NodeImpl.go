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
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
)

type NodeImpl struct {
	Identifier           string
	StateStorage         StateStorage
	DataStorage          DataStorage
	DataForwardStrategy  DataForwardStrategy
	QueryForwardStrategy QueryForwardStrategy
	ConnectionStrategy   ConnectionStrategy
	Timer                Timer

	connectingPeers []Connector
	connectedPeers  []Connector
	pendingQueries  map[data.Key][]Pusher
}

func (this *NodeImpl) Id() string {
	return this.Identifier
}

func (this *NodeImpl) Equal(other general.Equaler) bool {
	connector, ok := other.(Connector)
	return ok && connector.Id() == this.Id()
}

func (this *NodeImpl) Startup() {
	this.pendingQueries = make(map[data.Key][]Pusher)
	this.assertConsistency()
	this.requestPeerConnections()
}

func (this *NodeImpl) ShutDown() {
	for _, peer := range append(this.connectingPeers, this.connectedPeers...) {
		peer.CloseConnectionWith(this)
	}
}

func (this *NodeImpl) RequestConnectionWith(peer Connector) {
	if general.Contains(this.connectedPeers, peer) {
		return
	}

	if this.isConnectionAcceptedWith(peer) {
		this.confirmConnectionWith(peer)
		this.acceptConnectionWith(peer)
	} else {
		this.refuseConnectionWith(peer)
	}
}

func (this *NodeImpl) CloseConnectionWith(peer Connector) {
	this.connectingPeers = removePeer(this.connectingPeers, peer)
	this.connectedPeers = removePeer(this.connectedPeers, peer)
}

func (this *NodeImpl) IsConnectionPendingWith(peer Connector) bool {
	return general.Contains(this.connectingPeers, peer)
}

func (this *NodeImpl) IsConnectedWith(peer Connector) bool {
	return general.Contains(this.connectedPeers, peer)
}

func (this *NodeImpl) Push(data *data.Chunk) {
	if data == nil {
		return
	}

	for _, p := range this.dataForwardPeers(data.Key) {
		p.Push(data)
	}

	this.DataStorage.ConsiderStorage(data)
	this.removePendingQuery(data.Key)
	// TODO Remove pending query after timeout
}

func (this *NodeImpl) Query(key data.Key, receiver Pusher) {
	queryResult := this.DataStorage.Query(key)
	switch queryResult {
	case nil:
		this.registerPendingQuery(key, receiver)
		this.forwardQuery(key)
	default:
		receiver.Push(queryResult)
	}
}

// Private

func (this *NodeImpl) isConnectionAcceptedWith(peer Connector) bool {
	return this.IsConnectionPendingWith(peer) ||
		this.ConnectionStrategy.IsConnectionAcceptedWith(peer)
}

func (this *NodeImpl) isConnectionPendingWith(peer Connector) bool {
	return general.Contains(this.connectingPeers, peer)
}

func (this *NodeImpl) refuseConnectionWith(peer Connector) {
	peer.CloseConnectionWith(this)
}

func (this *NodeImpl) acceptConnectionWith(peer Connector) {
	this.connectedPeers = append(this.connectedPeers, peer)
	this.connectingPeers = removePeer(this.connectingPeers, peer)
}

func removePeer(peers []Connector, peerToRemove Connector) []Connector {
	for i, p := range peers {
		if peerToRemove.Equal(p) {
			nPeers := len(peers) - 1
			peers[i] = peers[nPeers]
			peers = peers[:nPeers]
			break
		}
	}

	return peers
}

func (this *NodeImpl) confirmConnectionWith(peer Connector) {
	if !this.isConnectionPendingWith(peer) {
		peer.RequestConnectionWith(this)
	}
}

func (this *NodeImpl) dataForwardPeers(key data.Key) []Connector {
	forwardPeers := this.DataForwardStrategy.ForwardTargetsFor(this.connectedPeers, key)
	queryReceivers, _ := this.pendingQueries[key]
	return general.SetUnion(forwardPeers, queryReceivers).([]Connector)
}

func (this *NodeImpl) forwardQuery(key data.Key) {
	fwdPeers := this.QueryForwardStrategy.ForwardTargetsFor(this.connectedPeers, key)
	for _, p := range fwdPeers {
		p.Query(key, this)
	}
}

func (this *NodeImpl) registerPendingQuery(key data.Key, receiver Pusher) {
	queriesForKey := this.pendingQueries[key]
	queriesForKey = append(queriesForKey, receiver)
	this.pendingQueries[key] = queriesForKey
}

func (this *NodeImpl) removePendingQuery(key data.Key) {
	delete(this.pendingQueries, key)
}

func (this *NodeImpl) assertConsistency() {
	if this.StateStorage == nil {
		panic("No StateStorage")
	}
	if this.DataStorage == nil {
		panic("No DataStorage")
	}
	if this.DataForwardStrategy == nil {
		panic("No DataForwardStrategy")
	}
	if this.ConnectionStrategy == nil {
		panic("No ConnectionStrategy")
	}
	if this.Timer == nil {
		panic("No Timer")
	}
}

func (this *NodeImpl) requestPeerConnections() {
	allPeers := this.StateStorage.GetAllKnownPeers()
	this.connectingPeers = this.ConnectionStrategy.PeersToConnect(allPeers)
	this.connectedPeers = []Connector{}
	for _, peer := range this.connectingPeers {
		peer.RequestConnectionWith(this)
	}
}
