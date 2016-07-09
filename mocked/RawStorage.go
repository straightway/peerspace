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

package mocked

import (
	"fmt"
	"math"
	"sort"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/storage"
	"github.com/stretchr/testify/mock"
)

type RawStorage struct {
	Base
	CurrentFreeStorage int
	Data               []storage.DataRecord
	Timer              peer.Timer
}

func NewRawStorage(timer peer.Timer) *RawStorage {
	result := &RawStorage{CurrentFreeStorage: math.MaxInt32, Timer: timer}
	result.On("FreeStorage").Return()
	result.On("SizeOf", mock.Anything).Return()
	result.On("Store", mock.Anything, mock.Anything, mock.Anything).Return()
	result.On("Delete", mock.Anything).Return()
	result.On("Query", mock.Anything).Return()
	result.On("LeastImportantData").Return()
	result.On("ExpiredData").Return()
	result.On("RePrioritize", mock.Anything, mock.Anything, mock.Anything).Return()
	return result
}

func (m *RawStorage) FreeStorage() int {
	m.Called()
	return m.CurrentFreeStorage
}

func (m *RawStorage) SizeOf(chunk *data.Chunk) int {
	m.Called(chunk)
	return len(chunk.Data)
}

func (m *RawStorage) Store(chunk *data.Chunk, priority float32, prioExpirationTime time.Time) {
	m.Called(chunk, priority, prioExpirationTime)
	m.deleteInternal(chunk.Key)
	chunkSize := m.SizeOf(chunk)
	if m.CurrentFreeStorage < chunkSize {
		panic(fmt.Sprintf(
			"Cannot store chunk %+v (size: %v). Free space: %v",
			chunk.Key,
			chunkSize,
			m.FreeStorage))
	}
	m.CurrentFreeStorage -= chunkSize
	record := storage.DataRecord{
		Chunk:              chunk,
		Priority:           priority,
		PrioExpirationTime: prioExpirationTime}

	m.Data = append(m.Data, record)
	sort.Sort(storage.DataRecordByPriority(m.Data))
}

func (m *RawStorage) RePrioritize(key data.Key, priority float32, prioExpirationTime time.Time) {
	m.Called(key, priority, prioExpirationTime)
	for i, r := range m.Data {
		if r.Chunk.Key != key {
			continue
		}

		r.Priority = priority
		r.PrioExpirationTime = prioExpirationTime
		m.Data[i] = r
		sort.Sort(storage.DataRecordByPriority(m.Data))
		return
	}

	panic(fmt.Sprintf("Cannot re-prioritize item with key %v as it is not contained", key))
}

func (m *RawStorage) Delete(key data.Key) int {
	m.Called(key)
	m.deleteInternal(key)
	return m.CurrentFreeStorage
}

func (m *RawStorage) Query(query peer.Query) []storage.DataRecord {
	m.Called(query)
	result := make([]storage.DataRecord, 0)
	for _, record := range m.Data {
		if query.Matches(record.Chunk.Key) {
			result = append(result, record)
		}
	}

	return result
}

func (m *RawStorage) LeastImportantData() general.Iterator {
	m.Called()
	return general.Iterate(m.Data)
}

func (m *RawStorage) ExpiredData() []storage.DataRecord {
	m.Called()
	result := make([]storage.DataRecord, 0)
	now := m.Timer.Time()
	for _, record := range m.Data {
		if record.PrioExpirationTime.Before(now) {
			result = append(result, record)
		}
	}

	return result
}

// Private

func (m *RawStorage) deleteInternal(key data.Key) {
	for i, record := range m.Data {
		if record.Chunk.Key == key {
			m.Data = append(m.Data[:i], m.Data[i+1:]...)
			m.CurrentFreeStorage += m.SizeOf(record.Chunk)
			break
		}
	}
}
