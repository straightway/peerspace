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
	"math"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
)

type Data struct {
	Configuration          *app.Configuration
	ConnectionInfoProvider ConnectionInfoProvider
	PeerDistanceCalculator PeerDistanceCalculator
}

func (this *Data) IsChunkAccepted(data *data.Chunk, origin id.Holder) bool {
	return uint64(len(data.Data)) <= this.Configuration.MaxChunkSize
}

func (this *Data) ForwardTargetsFor(key data.Key, origin id.Holder) []peer.Connector {
	var nearestPeer = this.nearestPeer(key)
	if nearestPeer != nil && origin.Id() != nearestPeer.Id() {
		return []peer.Connector{nearestPeer}
	} else {
		return []peer.Connector{}
	}
}

// Private

func (this *Data) nearestPeer(key data.Key) peer.Connector {
	var nearestPeer peer.Connector = nil
	var nearestPeerDistance uint64 = math.MaxUint64
	for _, peer := range this.ConnectionInfoProvider.ConnectedPeers() {
		currentDist := this.PeerDistanceCalculator.Distance(peer, key)
		if currentDist < nearestPeerDistance {
			nearestPeer = peer
			nearestPeerDistance = currentDist
		}
	}

	return nearestPeer
}
