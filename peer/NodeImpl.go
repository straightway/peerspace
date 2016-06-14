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
	StateStorage       StateStorage
	DataStorage        DataStorage
	ForwardStrategy    ForwardStrategy
	ConnectionStrategy ConnectionStrategy

	connectedPeers []Connector
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
	if this.ForwardStrategy == nil {
		panic("No ForwardStrategy")
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

func (this *NodeImpl) Push(data data.Chunk) {
	forwardPeers := this.ForwardStrategy.SelectedConnectors(this.connectedPeers)
	for _, p := range forwardPeers {
		p.Push(data)
	}
	this.DataStorage.ConsiderStorage(data)
}

func (this *NodeImpl) Query(key data.Key, receiver Connector) {
	queryResult := this.DataStorage.Query(key)
	println("peer.NodeImpl: queryResult = ", queryResult)
	if queryResult != nil {
		receiver.Push(queryResult)
	}
}
