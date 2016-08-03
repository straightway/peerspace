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
	"hash"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/ranges"
	"github.com/straightway/straightway/general/times"
	"github.com/straightway/straightway/peer"
)

type PeerDistanceRelated struct {
	Hasher      hash.Hash64
	Timer       peer.Timer
	LocalPeerId string
}

var expirationTimespans = []int{3650, 365, 30, 7, 1}

func (this *PeerDistanceRelated) Distance(peer peer.Connector, key data.Key) uint64 {
	return this.distanceIdToKey(peer.Id(), key)
}

func (this *PeerDistanceRelated) Distances(peer peer.Connector, query data.Query) []uint64 {
	result := make([]uint64, 0, 0)
	queryRange := ranges.Int64{query.TimeFrom, query.TimeTo + 1}
	for _, timestampRange := range this.timestampRangesForQuery(query) {
		if queryRange.IntersectsWith(timestampRange) {
			result = append(result, this.distanceForQueryTimeRange(peer, query, timestampRange))
		}
	}

	return result
}

func (this *PeerDistanceRelated) Priority(chunk *data.Chunk) (float32, time.Time) {
	peerDistance := this.distanceIdToKey(this.LocalPeerId, chunk.Key)
	if peerDistance == 0 {
		peerDistance = 1 // Avoid divison by zero
	}

	_, expirationTime := this.timestampAgeMarker(chunk.Key)
	return float32(this.Timer.Time().Unix()) / float32(peerDistance), expirationTime
}

// Private

func (this *PeerDistanceRelated) distanceForQueryTimeRange(
	peer peer.Connector, query data.Query, timepointRange ranges.Int64) uint64 {
	equivalentKey := data.Key{Id: query.Id, TimeStamp: timepointRange[1] - 1}
	return this.Distance(peer, equivalentKey)
}

func (this *PeerDistanceRelated) timestampRangesForQuery(query data.Query) []ranges.Int64 {
	timestampAges := this.timestampAgesForQuery(query)
	numRanges := len(timestampAges) - 2
	result := make([]ranges.Int64, 0, 0)
	for i := numRanges; 0 <= i; i-- {
		result = append(result, ranges.Int64{timestampAges[i], timestampAges[i+1] + 1})
	}

	return result
}

func (this *PeerDistanceRelated) timestampAgesForQuery(query data.Query) []int64 {
	timestampAges := []int64{0}
	if query.IsTimed() {
		timestampAges = append(timestampAges, this.timestampAgeTable()...)
		timestampAges = append(timestampAges, this.Timer.Time().Unix())
	} else {
		timestampAges = []int64{0, 0}
	}

	return timestampAges
}

func (this *PeerDistanceRelated) distanceIdToKey(peerId string, key data.Key) uint64 {
	this.Hasher.Reset()
	this.Hasher.Write([]byte(peerId))
	peerHash := this.Hasher.Sum64()

	this.Hasher.Reset()
	this.Hasher.Write([]byte(key.Id))
	if key.TimeStamp != 0 {
		ageMarker, _ := this.timestampAgeMarker(key)
		this.Hasher.Write([]byte{ageMarker})
	}

	keyHash := this.Hasher.Sum64()

	return peerHash ^ keyHash
}

func (this *PeerDistanceRelated) timestampAgeMarker(key data.Key) (byte, time.Time) {
	keyTimeStamp := key.TimeStamp
	ageTable := this.timestampAgeTable()
	expirationTime := times.Max()
	for i, timeStampFromTable := range ageTable {
		if keyTimeStamp <= timeStampFromTable {
			return byte(i + 1), expirationTime
		}
		expirationTime = time.Unix(key.TimeStamp, 0).In(time.UTC).
			Add(time.Duration(expirationTimespans[i]) * 24 * time.Hour)
	}

	return byte(len(ageTable) + 1), expirationTime
}

func (this *PeerDistanceRelated) timestampAgeTable() []int64 {
	now := this.Timer.Time()
	ages := make([]int64, len(expirationTimespans), len(expirationTimespans))
	for i := range expirationTimespans {
		ages[i] = now.AddDate(0, 0, -expirationTimespans[i]).Unix()
	}
	return ages
}
