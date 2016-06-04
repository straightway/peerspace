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
package core

import "fmt"

type PeerNode struct {
	stateStorage StateStorage
}

func (peerNode *PeerNode) Startup() {
	peers := peerNode.stateStorage.GetAllKnownPeers()
	for _, peer := range peers {
		peer.Connect(peerNode)
	}
}

func (peerNode *PeerNode) SetStateStorage(stateStorage StateStorage) {
	if stateStorage == nil {
		panic("nil stateStorage")
	}
	if peerNode.stateStorage != nil {
		panic("stateStorage already set")
	}

	peerNode.stateStorage = stateStorage
}

func (peerNode *PeerNode) Connect(peer Peer) {
	fmt.Printf("Connecting %v to $v", peerNode, peer)
}

func (peerNode *PeerNode) Push(data Data) {
	firstKnownPeer := peerNode.stateStorage.GetAllKnownPeers()[0]
	firstKnownPeer.Push(data)
}
