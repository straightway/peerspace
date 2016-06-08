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
	stateStorage    StateStorage
	forwardStrategy ForwardStrategy
}

func NewNode(s StateStorage, fs ForwardStrategy) Node {
	if s == nil {
		panic("nil StateStorage")
	}
	return &node{stateStorage: s, forwardStrategy: fs}
}

func (node *node) Startup() {
	peers := node.stateStorage.GetAllKnownPeers()
	for _, peer := range peers {
		peer.Connect(node)
	}
}

func (node *node) Connect(peer Connector) {
	fmt.Printf("Connecting %v to $v", node, peer)
}

func (node *node) Push(data Data) {
	forwardPeers := node.forwardStrategy.ForwardedPeer(node.stateStorage.GetAllKnownPeers())
	for _, p := range forwardPeers {
		p.Push(data)
	}
}
