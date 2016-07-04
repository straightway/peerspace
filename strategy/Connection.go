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

package strategy

import (
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
)

type Connection struct {
	Configuration          *peer.Configuration
	ConnectionInfoProvider ConnectionInfoProvider
}

func (this *Connection) PeersToConnect(allPeers []peer.Connector) []peer.Connector {
	filteredPeers := this.existingConnectionsFilteredFrom(allPeers)
	if len(filteredPeers) <= this.Configuration.MaxConnections {
		return filteredPeers
	} else {
		return filteredPeers[0:this.Configuration.MaxConnections]
	}
}

func (this *Connection) existingConnectionsFilteredFrom(allPeers []peer.Connector) []peer.Connector {
	filteredPeers := append([]peer.Connector(nil), allPeers...)
	omittedPeers := this.ConnectionInfoProvider.ConnectedPeers()
	omittedPeers = append(omittedPeers, this.ConnectionInfoProvider.ConnectingPeers()...)
	return general.RemoveItemsIf(filteredPeers, func(item interface{}) bool {
		return general.Contains(omittedPeers, item.(general.Equaler))
	}).([]peer.Connector)
}
