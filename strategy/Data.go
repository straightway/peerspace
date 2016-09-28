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
	"sort"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/peer"
)

type Data struct {
	Configuration          *app.Configuration
	ConnectionInfoProvider ConnectionInfoProvider
	PeerDistanceCalculator PeerDistanceCalculator
}

type peersByDistance struct {
	peers                  []peer.Connector
	peerDistanceCalculator PeerDistanceCalculator
	key                    data.Key
}

func (a peersByDistance) Len() int      { return len(a.peers) }
func (a peersByDistance) Swap(i, j int) { a.peers[i], a.peers[j] = a.peers[j], a.peers[i] }
func (a peersByDistance) Less(i, j int) bool {
	return a.peerDistanceCalculator.Distance(a.peers[i], a.key) < a.peerDistanceCalculator.Distance(a.peers[j], a.key)
}

func (this *Data) IsChunkAccepted(data *data.Chunk, origin id.Holder) bool {
	return uint64(len(data.Data)) <= this.Configuration.MaxChunkSize
}

func (this *Data) ForwardTargetsFor(key data.Key, origin id.Holder) []peer.Pusher {
	nearestPeers := slice.RemoveItemsIf(
		this.connectdPeersSortedByDistance(key),
		func(item interface{}) bool {
			peer := item.(id.Holder)
			return peer.Id() == origin.Id()
		}).([]peer.Connector)

	numItems := int(this.Configuration.ForwardNodes)
	if len(nearestPeers) <= numItems {
		numItems = len(nearestPeers)
	}

	return slice.Cast(nearestPeers, []peer.Pusher{}).([]peer.Pusher)[:numItems]
}

// Private

func (this *Data) connectdPeersSortedByDistance(key data.Key) []peer.Connector {
	sortablePeers := peersByDistance{
		peers: this.ConnectionInfoProvider.ConnectedPeers(),
		peerDistanceCalculator: this.PeerDistanceCalculator,
		key: key}
	sort.Sort(sortablePeers)
	return sortablePeers.peers
}
