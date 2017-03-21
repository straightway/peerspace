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

package simc

import (
	"bytes"
	"encoding/binary"
	"fmt"
	"sort"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/iter"
	"github.com/straightway/straightway/general/times"
)

type RawStorage struct {
	freeStorageValue uint64
	timer            times.Provider
	storedData       []data.Record
	storedDataByKey  map[data.Key]data.Record
}

func NewRawStorage(freeStorage uint64, timer times.Provider) *RawStorage {
	return &RawStorage{
		freeStorageValue: freeStorage,
		timer:            timer,
		storedDataByKey:  make(map[data.Key]data.Record)}
}

func (this *RawStorage) CreateChunk(key data.Key, virtualSize uint64) *data.Chunk {
	buf := new(bytes.Buffer)
	binary.Write(buf, binary.LittleEndian, virtualSize)
	return &data.Chunk{Key: key, Data: buf.Bytes()}
}

func (this *RawStorage) FreeStorage() uint64 {
	return this.freeStorageValue
}

func (this *RawStorage) SizeOf(chunk *data.Chunk) uint64 {
	buf := bytes.NewReader(chunk.Data)
	var virtualSize uint64 = 0
	err := binary.Read(buf, binary.LittleEndian, &virtualSize)
	if err != nil {
		panic(err)
	}
	return virtualSize
}

func (this *RawStorage) Store(chunk *data.Chunk, priority float32, prioExpirationTime time.Time) {
	this.Delete(chunk.Key)

	this.freeStorageValue -= this.SizeOf(chunk)
	dataRecord := data.Record{
		Chunk:              chunk,
		Priority:           priority,
		PrioExpirationTime: prioExpirationTime}
	this.storedData = append(this.storedData, dataRecord)
	sort.Sort(data.RecordByPriority(this.storedData))
	this.storedDataByKey[chunk.Key] = dataRecord
}

func (this *RawStorage) Delete(key data.Key) {
	_, isStored := this.storedDataByKey[key]
	if isStored == false {
		return
	}

	delete(this.storedDataByKey, key)
	for i, dataRecord := range this.storedData {
		if dataRecord.Chunk.Key == key {
			this.freeStorageValue += this.SizeOf(dataRecord.Chunk)
			this.storedData = append(this.storedData[:i], this.storedData[i+1:]...)
			return
		}
	}
}

func (this *RawStorage) Query(query data.Query) []data.Record {
	result := this.tryMatchingUntimedQuery(query)
	if 0 < len(result) {
		return result
	}

	for _, record := range this.storedData {
		if query.Matches(record.Chunk.Key) {
			result = append(result, record)
		}
	}

	return result
}

func (this *RawStorage) LeastImportantData() iter.Ator {
	return iter.OnSlice(data.SelectChunks(this.storedData))
}

func (this *RawStorage) ExpiredData() []data.Record {
	result := make([]data.Record, 0, 0)
	now := this.timer.Time()
	for _, record := range this.storedData {
		if !now.Before(record.PrioExpirationTime) {
			result = append(result, record)
		}
	}

	return result
}

func (this *RawStorage) RePrioritize(key data.Key, prio float32, prioExpTime time.Time) {
	for _, record := range this.storedData {
		if record.Chunk.Key == key {
			this.Delete(key)
			this.Store(record.Chunk, prio, prioExpTime)
			return
		}
	}

	panic(fmt.Sprintf("key %+v not found", key))
}

// Private

func (this *RawStorage) tryMatchingUntimedQuery(query data.Query) []data.Record {
	result := make([]data.Record, 0, 1)
	if query.IsTimed() == false {
		match, isFound := this.storedDataByKey[data.Key{Id: query.Id, TimeStamp: query.TimeTo}]
		if isFound {
			result = append(result, match)
		}
	}

	return result
}
