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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
)

type Data struct {
	RawStorage Raw
}

func (this *Data) Startup() {}

func (this *Data) ConsiderStorage(chunk *data.Chunk) {
	keysToDelete, success := this.getChunkKeysToFreeStorage(this.RawStorage.GetSizeOf(chunk))
	if success {
		this.deleteKeys(keysToDelete)
		this.RawStorage.Store(chunk, 0.0)
	}
}

func (this *Data) Query(query peer.Query) []*data.Chunk {
	queryResult := this.RawStorage.Query(query)
	return ToChunkSlice(queryResult)
}

// Private

func (this *Data) getChunkKeysToFreeStorage(chunkSize int) (keysToDelete []data.Key, success bool) {
	freeStorage := this.RawStorage.GetFreeStorage()
	if freeStorage < chunkSize {
		this.RawStorage.GetLeastImportantData().Loop(func(item interface{}) general.LoopControl {
			chunk := item.(DataRecord).Chunk
			keysToDelete = append(keysToDelete, chunk.Key)
			freeStorage += this.RawStorage.GetSizeOf(chunk)
			return general.BreakIf(chunkSize <= freeStorage)
		})
	}

	return keysToDelete, chunkSize <= freeStorage
}

func (this *Data) deleteKeys(keysToDelete []data.Key) {
	for _, key := range keysToDelete {
		this.RawStorage.Delete(key)
	}
}
