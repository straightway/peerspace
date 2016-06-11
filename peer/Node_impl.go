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

type node struct {
	stateStorage       StateStorage
	forwardStrategy    ForwardStrategy
	connectionStrategy ConnectionStrategy
}

func NewNode(s StateStorage, fs ForwardStrategy, cs ConnectionStrategy) Node {
	if s == nil {
		panic("nil StateStorage")
	}
	if fs == nil {
		panic("nil ForwardStrategy")
	}
	if cs == nil {
		panic("nil ConnectionStrategy")
	}

	return &node{
		stateStorage:       s,
		forwardStrategy:    fs,
		connectionStrategy: cs}
}

func (node *node) Startup() {
	allPeers := node.stateStorage.GetAllKnownPeers()
	peers := node.connectionStrategy.SelectedConnectors(allPeers)
	for _, peer := range peers {
		peer.Connect(node)
	}
}

func (node *node) Connect(peer Connector) {
	fmt.Printf("Connecting %v to %v", node, peer)
}

func (node *node) Push(data Data) {
	forwardPeers := node.forwardStrategy.SelectedConnectors(node.stateStorage.GetAllKnownPeers())
	for _, p := range forwardPeers {
		p.Push(data)
	}
}
