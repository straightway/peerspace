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
	StateStorage         StateStorage
	DataStorage          DataStorage
	DataForwardStrategy  DataForwardStrategy
	QueryForwardStrategy QueryForwardStrategy
	ConnectionStrategy   ConnectionStrategy
	Identifier           string

	connectedPeers  []Connector
	pendingQueryMap map[data.Key][]Pusher
}

func (this *NodeImpl) Id() string {
	return this.Identifier
}

func (this *NodeImpl) Equal(other general.Equaler) bool {
	connector, ok := other.(Connector)
	return ok && connector.Id() == this.Id()
}

func (this *NodeImpl) Startup() {
	//general.AssertFieldsNotNil(this, "StateStorage", "DataStorage", "DataForwardStrategy", "ConnectionStrategy")
	this.assertConsistency()
	this.requestPeerConnections()
}

func (this *NodeImpl) ShutDown() {
	for _, peer := range this.connectedPeers {
		peer.CloseConnectionWith(this)
	}
}

func (this *NodeImpl) RequestConnectionWith(peer Connector) {
	if general.Contains(this.connectedPeers, peer) {
		return
	}

	// TODO if the connection is refused, it should not be added to connectedPeers
	this.connectedPeers = append(this.connectedPeers, peer)
	if this.ConnectionStrategy.IsConnectionAcceptedWith(peer) {
		peer.RequestConnectionWith(this)
	} else {
		peer.CloseConnectionWith(this)
	}
}

func (this *NodeImpl) CloseConnectionWith(peer Connector) {
	panic("Not implemented")
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

func (this *NodeImpl) dataForwardPeers(key data.Key) []Connector {
	// TODO Data forwarding peers depends on data
	forwardPeers := this.DataForwardStrategy.SelectedConnectors(this.connectedPeers)
	queryReceivers, _ := this.pendingQueries()[key]
	return general.SetUnion(forwardPeers, queryReceivers).([]Connector)
}

func (this *NodeImpl) forwardQuery(key data.Key) {
	// TODO Data forwarding queries depends on data
	fwdPeers := this.QueryForwardStrategy.SelectedConnectors(this.connectedPeers)
	for _, p := range fwdPeers {
		p.Query(key, this)
	}
}

func (this *NodeImpl) registerPendingQuery(key data.Key, receiver Pusher) {
	queriesForKey := this.pendingQueries()[key]
	queriesForKey = append(queriesForKey, receiver)
	this.pendingQueries()[key] = queriesForKey
}

func (this *NodeImpl) removePendingQuery(key data.Key) {
	delete(this.pendingQueries(), key)
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
}

func (this *NodeImpl) requestPeerConnections() {
	allPeers := this.StateStorage.GetAllKnownPeers()
	this.connectedPeers = this.ConnectionStrategy.SelectedConnectors(allPeers)
	for _, peer := range this.connectedPeers {
		peer.RequestConnectionWith(this)
	}

	// TODO Handle requested but mot yet confirmed connections
}

func (this *NodeImpl) pendingQueries() map[data.Key][]Pusher {
	if this.pendingQueryMap == nil {
		this.pendingQueryMap = make(map[data.Key][]Pusher)
	}

	return this.pendingQueryMap
}
