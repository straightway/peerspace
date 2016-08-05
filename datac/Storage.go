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

package datac

import (
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/general/loop"
)

type Storage struct {
	PriorityGenerator data.PriorityGenerator
	RawStorage        data.RawStorage
}

func (this *Storage) Startup() {
	lifeCycle, ok := this.RawStorage.(general.LifeCycle)
	if ok {
		lifeCycle.Startup()
	}
}

func (this *Storage) ShutDown() {
	lifeCycle, ok := this.RawStorage.(general.LifeCycle)
	if ok {
		lifeCycle.ShutDown()
	}
}

func (this *Storage) ConsiderStorage(chunk *data.Chunk) {
	this.rePrioritizeChunksWithExpiredPrio()
	keysToDelete, success := this.getChunkKeysToFreeStorage(this.RawStorage.SizeOf(chunk))
	if success {
		this.deleteKeys(keysToDelete)
		prio, expiration := this.PriorityGenerator.Priority(chunk)
		this.RawStorage.Store(chunk, prio, expiration)
	}
}

func (this *Storage) Query(query data.Query) []*data.Chunk {
	queryResult := this.RawStorage.Query(query)
	for _, data := range queryResult {
		prio, expiration := this.PriorityGenerator.Priority(data.Chunk)
		this.RawStorage.RePrioritize(data.Chunk.Key, prio, expiration)
	}
	return data.SelectChunks(queryResult)
}

// Private

func (this *Storage) rePrioritizeChunksWithExpiredPrio() {
	chunksWithExpiredPrio := this.RawStorage.ExpiredData()
	for _, chunk := range chunksWithExpiredPrio {
		prio, expiration := this.PriorityGenerator.Priority(chunk.Chunk)
		this.RawStorage.RePrioritize(chunk.Chunk.Key, prio, expiration)
	}
}

func (this *Storage) getChunkKeysToFreeStorage(chunkSize uint64) (keysToDelete []data.Key, success bool) {
	freeStorage := this.RawStorage.FreeStorage()
	if chunkSize <= freeStorage {
		return nil, true
	}

	this.RawStorage.LeastImportantData().Loop(func(item interface{}) loop.Control {
		chunk := item.(data.Record).Chunk
		keysToDelete = append(keysToDelete, chunk.Key)
		freeStorage += this.RawStorage.SizeOf(chunk)
		return loop.BreakIf(chunkSize <= freeStorage)
	})

	return keysToDelete, chunkSize <= freeStorage
}

func (this *Storage) deleteKeys(keysToDelete []data.Key) {
	for _, key := range keysToDelete {
		this.RawStorage.Delete(key)
	}
}
