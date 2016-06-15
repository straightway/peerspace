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

import "github.com/straightway/straightway/data"

type NodeImpl struct {
	StateStorage         StateStorage
	DataStorage          DataStorage
	DataForwardStrategy  DataForwardStrategy
	QueryForwardStrategy QueryForwardStrategy
	ConnectionStrategy   ConnectionStrategy

	connectedPeers  []Connector
	pendingQueryMap map[data.Key][]Pusher
}

func (this *NodeImpl) Id() string {
	panic("Not implemented")
}

func (this *NodeImpl) Startup() {
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

	allPeers := this.StateStorage.GetAllKnownPeers()
	this.connectedPeers = this.ConnectionStrategy.SelectedConnectors(allPeers)
	for _, peer := range this.connectedPeers {
		peer.RequestConnectionWith(this)
	}
}

func (this *NodeImpl) ShutDown() {
	for _, peer := range this.connectedPeers {
		peer.CloseConnectionWith(this)
	}
}

func (this *NodeImpl) RequestConnectionWith(peer Connector) {
	for _, p := range this.connectedPeers {
		if p.Id() == peer.Id() {
			return
		}
	}
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
	forwardPeers := this.DataForwardStrategy.SelectedConnectors(this.connectedPeers)
	for _, p := range forwardPeers {
		p.Push(data)
	}
	this.DataStorage.ConsiderStorage(data)
	if receivers, ok := this.pendingQueries()[data.Key]; ok {
		for _, receiver := range receivers {
			receiver.Push(data)
		}
		// TODO Remove pending query now
		// TODO Remove pending query after timeout
	}
}

func (this *NodeImpl) Query(key data.Key, receiver Pusher) {
	queryResult := this.DataStorage.Query(key)
	switch queryResult {
	case nil:
		queriesForKey := this.pendingQueries()[key]
		queriesForKey = append(queriesForKey, receiver)
		this.pendingQueries()[key] = queriesForKey
		fwdPeers := this.QueryForwardStrategy.SelectedConnectors(this.connectedPeers)
		for _, p := range fwdPeers {
			p.Query(key, this)
		}
	default:
		receiver.Push(queryResult)
	}
}

func (this *NodeImpl) pendingQueries() map[data.Key][]Pusher {
	if this.pendingQueryMap == nil {
		this.pendingQueryMap = make(map[data.Key][]Pusher)
	}
	return this.pendingQueryMap
}
