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

package data

import (
	"fmt"
	"math"
	"sort"
	"time"

	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general/iter"
	"github.com/straightway/straightway/general/mocked"
	"github.com/straightway/straightway/general/times"
)

type RawStorageMock struct {
	mocked.Base
	CurrentFreeStorage uint64
	Data               []Record
	Timer              times.Provider
}

func NewRawStorageMock(timer times.Provider) *RawStorageMock {
	result := &RawStorageMock{CurrentFreeStorage: math.MaxInt32, Timer: timer}
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

func (m *RawStorageMock) FreeStorage() uint64 {
	m.Called()
	return m.CurrentFreeStorage
}

func (m *RawStorageMock) SizeOf(chunk *Chunk) uint64 {
	m.Called(chunk)
	return uint64(len(chunk.Data))
}

func (m *RawStorageMock) Store(chunk *Chunk, priority float32, prioExpirationTime time.Time) {
	m.Called(chunk, priority, prioExpirationTime)
	m.deleteInternal(chunk.Key)
	chunkSize := m.SizeOf(chunk)
	if m.CurrentFreeStorage < chunkSize {
		panic(fmt.Sprintf(
			"Cannot store chunk %+v (size: %v). Free space: %v",
			chunk.Key,
			chunkSize,
			m.FreeStorage()))
	}
	m.CurrentFreeStorage -= chunkSize
	record := Record{
		Chunk:              chunk,
		Priority:           priority,
		PrioExpirationTime: prioExpirationTime}

	m.Data = append(m.Data, record)
	sort.Sort(RecordByPriority(m.Data))
}

func (m *RawStorageMock) RePrioritize(key Key, priority float32, prioExpirationTime time.Time) {
	m.Called(key, priority, prioExpirationTime)
	for i, r := range m.Data {
		if r.Chunk.Key != key {
			continue
		}

		r.Priority = priority
		r.PrioExpirationTime = prioExpirationTime
		m.Data[i] = r
		sort.Sort(RecordByPriority(m.Data))
		return
	}

	panic(fmt.Sprintf("Cannot re-prioritize item with key %v as it is not contained", key))
}

func (m *RawStorageMock) Delete(key Key) {
	m.Called(key)
	m.deleteInternal(key)
}

func (m *RawStorageMock) Query(query Query) []Record {
	m.Called(query)
	result := make([]Record, 0)
	for _, record := range m.Data {
		if query.Matches(record.Chunk.Key) {
			result = append(result, record)
		}
	}

	return result
}

func (m *RawStorageMock) LeastImportantData() iter.Ator {
	m.Called()
	return iter.OnSlice(m.Data)
}

func (m *RawStorageMock) ExpiredData() []Record {
	m.Called()
	result := make([]Record, 0)
	now := m.Timer.Time()
	for _, record := range m.Data {
		if record.PrioExpirationTime.Before(now) {
			result = append(result, record)
		}
	}

	return result
}

// Private

func (m *RawStorageMock) deleteInternal(key Key) {
	for i, record := range m.Data {
		if record.Chunk.Key == key {
			m.Data = append(m.Data[:i], m.Data[i+1:]...)
			m.CurrentFreeStorage += m.SizeOf(record.Chunk)
			break
		}
	}
}
