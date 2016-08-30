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

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/peer"
)

type Query struct {
	ConnectionInfoProvider ConnectionInfoProvider
	PeerDistanceCalculator PeerDistanceCalculator
	Configuration          *app.Configuration
}

func (this *Query) IsQueryAccepted(query data.Query, receiver peer.Pusher) bool {
	return true
}

func (this *Query) ForwardTargetsFor(query data.Query, receiver peer.PusherWithId) []peer.Queryable {
	var allConnections []peer.QueryableWithId
	allConnections = slice.Cast(
		this.ConnectionInfoProvider.ConnectedPeers(),
		allConnections).([]peer.QueryableWithId)

	seletor := nearestPeerSelector{Query: query, PeerDistanceCalculator: this.PeerDistanceCalculator}
	nearestPeers := seletor.NearestPeers(allConnections)

	result := slice.SetUnion(slice.RemoveItemsIf(nearestPeers, func(item interface{}) bool {
		return item.(general.Equaler).Equal(receiver)
	})).([]peer.QueryableWithId)

	return slice.Cast(result, ([]peer.Queryable)(nil)).([]peer.Queryable)
}

func (this *Query) TimeoutFor(query data.Query) time.Duration {
	if query.IsTimed() {
		return this.Configuration.TimedQueryTimeout
	} else {
		return this.Configuration.UntimedQueryTimeout
	}
}

// Private

type nearestPeerSelector struct {
	Query                  data.Query
	PeerDistanceCalculator PeerDistanceCalculator
	nearestPeers           []peer.QueryableWithId
	nearestPeerDistances   []uint64
}

func (this *nearestPeerSelector) NearestPeers(allConnections []peer.QueryableWithId) []peer.QueryableWithId {
	for _, p := range allConnections {
		this.updateNearestPeers(p)
	}

	return this.nearestPeers
}

func (this *nearestPeerSelector) updateNearestPeers(peer peer.QueryableWithId) {
	distances := this.PeerDistanceCalculator.Distances(peer, this.Query)
	this.initializeNearestPeers(len(distances))
	this.exchangeIfNearer(peer, distances)
}

func (this *nearestPeerSelector) exchangeIfNearer(peer peer.QueryableWithId, distances []uint64) {
	for i, d := range distances {
		if d < this.nearestPeerDistances[i] {
			this.nearestPeers[i] = peer
			this.nearestPeerDistances[i] = d
		}
	}
}

func (this *nearestPeerSelector) initializeNearestPeers(count int) {
	if this.nearestPeers != nil {
		return
	}

	this.nearestPeers = make([]peer.QueryableWithId, count, count)
	this.nearestPeerDistances = make([]uint64, count, count)
	for i := range this.nearestPeerDistances {
		this.nearestPeerDistances[i] = math.MaxUint64
	}
}
