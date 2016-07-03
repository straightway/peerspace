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

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/storage"
	"github.com/stretchr/testify/mock"
)

type RawStorage struct {
	Base
	FreeStorage int
	Data        []storage.DataRecord
}

func NewRawStorage() *RawStorage {
	result := &RawStorage{FreeStorage: math.MaxInt32}
	result.On("GetFreeStorage").Return()
	result.On("GetSizeOf", mock.Anything).Return()
	result.On("Store", mock.Anything, mock.Anything).Return()
	result.On("Delete", mock.Anything).Return()
	result.On("Query", mock.Anything).Return()
	result.On("GetLeastImportantData").Return()
	return result
}

func (m *RawStorage) GetFreeStorage() int {
	m.Called()
	return m.FreeStorage
}

func (m *RawStorage) GetSizeOf(chunk *data.Chunk) int {
	m.Called(chunk)
	return len(chunk.Data)
}

func (m *RawStorage) Store(chunk *data.Chunk, priority float32) {
	m.Called(chunk, priority)
	chunkSize := m.GetSizeOf(chunk)
	if m.FreeStorage < chunkSize {
		panic(fmt.Sprintf("Cannot store chunk %+v (size: %v). Free space: %v", chunk.Key, chunkSize, m.FreeStorage))
	}
	m.FreeStorage -= chunkSize
	record := storage.DataRecord{Chunk: chunk, Priority: priority}
	for i, context := range m.Data {
		if priority < context.Priority {
			m.Data = append(m.Data, record)
			copy(m.Data[i+1:], m.Data[i:])
			m.Data[i] = record
			return
		}
	}
	m.Data = append(m.Data, record)
}

func (m *RawStorage) Delete(key data.Key) int {
	m.Called(key)
	for i, record := range m.Data {
		if record.Chunk.Key == key {
			m.Data = append(m.Data[:i], m.Data[i+1:]...)
			m.FreeStorage += m.GetSizeOf(record.Chunk)
			break
		}
	}

	return m.FreeStorage
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

func (m *RawStorage) GetLeastImportantData() general.Iterator {
	m.Called()
	return general.Iterate(m.Data)
}
