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
	"time"

	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
)

type Query struct {
	ConnectionInfoProvider ConnectionInfoProvider
	PeerDistanceCalculator PeerDistanceCalculator
	Configuration          *peer.Configuration
}

func (this *Query) IsQueryAccepted(query peer.Query, receiver peer.Pusher) bool {
	return true
}

func (this *Query) ForwardTargetsFor(query peer.Query, receiver peer.Pusher) []peer.Connector {
	allConnections := this.ConnectionInfoProvider.ConnectedPeers()

	var nearedPeer []peer.Connector
	var nearedPeerDistance []uint64

	for _, p := range allConnections {
		peerDistances := this.PeerDistanceCalculator.Distances(p, query)
		if nearedPeer == nil {
			nearedPeer = make([]peer.Connector, len(peerDistances), len(peerDistances))
			nearedPeerDistance = make([]uint64, len(peerDistances), len(peerDistances))
			for i := range nearedPeerDistance {
				nearedPeerDistance[i] = math.MaxUint64
			}
		}
		for i, d := range peerDistances {
			if d < nearedPeerDistance[i] {
				nearedPeer[i] = p
				nearedPeerDistance[i] = d
			}
		}
	}

	return general.SetUnion(general.RemoveItemsIf(nearedPeer, func(item interface{}) bool {
		return item.(peer.Connector).Equal(receiver)
	})).([]peer.Connector)
}

func (this *Query) TimeoutFor(query peer.Query) time.Duration {
	if query.IsTimed() {
		return this.Configuration.TimedQueryTimeout
	} else {
		return this.Configuration.UntimedQueryTimeout
	}
}
