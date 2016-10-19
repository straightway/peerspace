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
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/id"
	"github.com/straightway/straightway/peer"
)

type distanceCalculator struct {
	distances map[string]uint64
}

func (m distanceCalculator) Distance(peer id.Holder, key data.Key) uint64 {
	return m.distances[peer.Id()]
}

func (m distanceCalculator) Distances(peer id.Holder, query data.Query) []uint64 {
	panic("Not supported")
}

func Test_Data_Sorting(t *testing.T) {
	distanceCalculator := distanceCalculator{distances: make(map[string]uint64)}
	toSort := peersByDistance{
		peers: []peer.Connector{
			peer.NewConnectorMock(),
			peer.NewConnectorMock(),
			peer.NewConnectorMock(),
			peer.NewConnectorMock()},
		peerDistanceCalculator: distanceCalculator,
		key: data.UntimedKey}
	distanceCalculator.distances[toSort.peers[0].Id()] = 0
	distanceCalculator.distances[toSort.peers[1].Id()] = 3
	distanceCalculator.distances[toSort.peers[2].Id()] = 1
	distanceCalculator.distances[toSort.peers[3].Id()] = 2
	expectedOrder := []peer.Connector{toSort.peers[0], toSort.peers[2], toSort.peers[3], toSort.peers[1]}
	sort.Sort(toSort)
	assert.Equal(t, expectedOrder, toSort.peers)
}
