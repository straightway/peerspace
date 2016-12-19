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

package simc

import (
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
)

type stateStorage struct {
	connectors   []peer.Connector
	connectorIds map[id.Type]bool
}

func NewStateStorage(connectors ...peer.Connector) peer.StateStorage {
	result := &stateStorage{connectorIds: make(map[id.Type]bool)}
	for _, connector := range connectors {
		result.AddKnownPeer(connector)
	}

	return result
}

func (this *stateStorage) GetAllKnownPeers() []peer.Connector {
	return this.connectors
}

func (this *stateStorage) IsKnownPeer(peer peer.Connector) bool {
	_, found := this.connectorIds[peer.Id()]
	return found
}

func (this *stateStorage) AddKnownPeer(peer peer.Connector) {
	if this.IsKnownPeer(peer) == false {
		this.connectors = append(this.connectors, peer)
		this.connectorIds[peer.Id()] = true
	}
}
