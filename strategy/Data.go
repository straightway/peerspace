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
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
)

type Data struct {
	Configuration          *peer.Configuration
	ConnectionInfoProvider ConnectionInfoProvider
}

func (this *Data) IsChunkAccepted(data *data.Chunk, origin peer.Connector) bool {
	return len(data.Data) <= this.Configuration.MaxChunkSize
}

func (this *Data) ForwardTargetsFor(key data.Key, origin peer.Connector) []peer.Connector {
	result := append([]peer.Connector(nil), this.ConnectionInfoProvider.ConnectedPeers()...)
	return general.RemoveItemsIf(result, func(item interface{}) bool {
		return origin.Equal(item.(general.Equaler))
	}).([]peer.Connector)
}
