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

package storage

import (
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"
)

type Data struct {
	RawStorage Raw
}

func chunkOrder(a, b *data.Key) bool {
	panic("Not implemented")
}

func (this *Data) Startup() {
	this.RawStorage.SetChunkOrder(chunkOrder)
}

func (this *Data) ConsiderStorage(chunk *data.Chunk) {
	this.RawStorage.Store(chunk)
}

func (this *Data) Query(query peer.Query) []*data.Chunk {
	return this.RawStorage.Query(query)
}
