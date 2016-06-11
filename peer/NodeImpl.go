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

import "fmt"

type NodeImpl struct {
	StateStorage       StateStorage
	DataStorage        DataStorage
	ForwardStrategy    ForwardStrategy
	ConnectionStrategy ConnectionStrategy

	connectedPeers []Connector
}

func (node *NodeImpl) Startup() {
	if node.StateStorage == nil {
		panic("No StateStorage")
	}
	if node.DataStorage == nil {
		panic("No DataStorage")
	}
	if node.ForwardStrategy == nil {
		panic("No ForwardStrategy")
	}
	if node.ConnectionStrategy == nil {
		panic("No ConnectionStrategy")
	}

	allPeers := node.StateStorage.GetAllKnownPeers()
	node.connectedPeers = node.ConnectionStrategy.SelectedConnectors(allPeers)
	for _, peer := range node.connectedPeers {
		peer.Connect(node)
	}
}

func (node *NodeImpl) Connect(peer Connector) {
	fmt.Printf("Connecting %v to %v", node, peer)
}

func (node *NodeImpl) Push(data Data) {
	forwardPeers := node.ForwardStrategy.SelectedConnectors(node.connectedPeers)
	for _, p := range forwardPeers {
		p.Push(data)
	}
	node.DataStorage.ConsiderStorage(data)
}
