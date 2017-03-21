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
	"math/rand"

	"github.com/straightway/straightway/app"
	"github.com/straightway/straightway/peer"
)

type Announcement struct {
	Configuration *app.Configuration
	RandomSource  rand.Source
	StateStorage  peer.StateStorage
}

func (this *Announcement) AnnouncedPeers() []peer.Connector {
	knownPeers := this.StateStorage.GetAllKnownPeers()
	if len(knownPeers) < this.Configuration.MaxAnnouncedPeers {
		return knownPeers
	} else {
		dice := rand.New(this.RandomSource)
		permutation := dice.Perm(len(knownPeers))[0:this.Configuration.MaxAnnouncedPeers]
		result := make([]peer.Connector, this.Configuration.MaxAnnouncedPeers, this.Configuration.MaxAnnouncedPeers)
		for i, j := range permutation {
			result[i] = knownPeers[j]
		}

		return result
	}
}
