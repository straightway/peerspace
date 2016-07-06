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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"
)

type PeerDistanceCalculatorImpl struct {
	Hasher hash.Hash64
	Timer  peer.Timer
}

func (this *PeerDistanceCalculatorImpl) Distance(peer peer.Connector, key data.Key) uint64 {
	this.Hasher.Reset()
	this.Hasher.Write([]byte(peer.Id()))
	peerHash := this.Hasher.Sum64()

	this.Hasher.Reset()
	this.Hasher.Write([]byte(key.Id))
	if key.TimeStamp != 0 {
		this.Hasher.Write(this.timestampAgeMarker(key))
	}

	keyHash := this.Hasher.Sum64()

	return peerHash ^ keyHash
}

// Private

func (this *PeerDistanceCalculatorImpl) timestampAgeMarker(key data.Key) []byte {
	keyTimeStamp := key.TimeStamp
	ageTable := this.timestampAgeTable()
	for i, timeStampFromTable := range ageTable {
		if keyTimeStamp <= timeStampFromTable {
			return []byte{byte(i + 1)}
		}
	}

	return []byte{byte(len(ageTable) + 1)}
}

func (this *PeerDistanceCalculatorImpl) timestampAgeTable() []int64 {
	now := this.Timer.Time()
	return []int64{
		now.AddDate(0, 0, -3650).Unix(),
		now.AddDate(0, 0, -365).Unix(),
		now.AddDate(0, 0, -30).Unix(),
		now.AddDate(0, 0, -7).Unix(),
		now.AddDate(0, 0, -1).Unix()}
}
