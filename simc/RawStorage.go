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
	"github.com/straightway/straightway/general/loop"
	"github.com/straightway/straightway/general/slice"
	"github.com/straightway/straightway/general/times"
)

type RawStorage struct {
	FreeStorageValue uint64
	Timer            times.Provider
	storedData       []data.Record
}

func (this *RawStorage) CreateChunk(key data.Key, virtualSize uint64) *data.Chunk {
	buf := new(bytes.Buffer)
	binary.Write(buf, binary.LittleEndian, virtualSize)
	return &data.Chunk{Key: key, Data: buf.Bytes()}
}

func (this *RawStorage) FreeStorage() uint64 {
	return this.FreeStorageValue
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
	this.FreeStorageValue -= this.SizeOf(chunk)
	dataRecord := data.Record{
		Chunk:              chunk,
		Priority:           priority,
		PrioExpirationTime: prioExpirationTime}
	this.storedData = append(this.storedData, dataRecord)
	sort.Sort(data.RecordByPriority(this.storedData))
}

func (this *RawStorage) Delete(key data.Key) {
	this.storedData = slice.RemoveItemsIf(this.storedData, func(item interface{}) bool {
		dataRecord := item.(data.Record)
		isFound := dataRecord.Chunk.Key == key
		if isFound {
			this.FreeStorageValue += this.SizeOf(dataRecord.Chunk)
		}
		return isFound
	}).([]data.Record)
}

func (this *RawStorage) Query(query data.Query) []data.Record {
	result := make([]data.Record, 0, 0)
	for _, record := range this.storedData {
		if query.Matches(record.Chunk.Key) {
			result = append(result, record)
		}
	}

	return result
}

func (this *RawStorage) LeastImportantData() loop.Iterator {
	return slice.Iterate(data.SelectChunks(this.storedData))
}

func (this *RawStorage) ExpiredData() []data.Record {
	result := make([]data.Record, 0, 0)
	now := this.Timer.Time()
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
