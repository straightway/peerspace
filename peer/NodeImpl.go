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

type NodeImpl struct {
	StateStorage       StateStorage
	DataStorage        DataStorage
	ForwardStrategy    ForwardStrategy
	ConnectionStrategy ConnectionStrategy

	connectedPeers []Connector
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
	this.connectedPeers = append(this.connectedPeers, peer)
	peer.NotifyConnectionAck(this)
}

func (m *NodeImpl) NotifyConnectionAck(peer Connector) {
	panic("Not implemented")
}

func (this *NodeImpl) CloseConnectionWith(peer Connector) {
	panic("Not implemented")
}

func (this *NodeImpl) Push(data Data) {
	forwardPeers := this.ForwardStrategy.SelectedConnectors(this.connectedPeers)
	for _, p := range forwardPeers {
		p.Push(data)
	}
	this.DataStorage.ConsiderStorage(data)
}
