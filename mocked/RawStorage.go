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
	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/storage"
	"github.com/stretchr/testify/mock"
)

type RawStorage struct {
	Base
	Data       []*data.Chunk
	ChunkOrder storage.ChunkOrder
}

func NewRawStorage() *RawStorage {
	result := &RawStorage{}
	result.On("Store", mock.Anything).Return()
	result.On("Query", mock.Anything).Return()
	result.On("SetChunkOrder", mock.Anything).Return()
	return result
}

func (m *RawStorage) Store(chunk *data.Chunk) {
	m.Called(chunk)
	m.Data = append(m.Data, chunk)
}

func (m *RawStorage) Query(query peer.Query) []*data.Chunk {
	m.Called(query)
	result := make([]*data.Chunk, 0)
	for _, chunk := range m.Data {
		if query.Matches(chunk.Key) {
			result = append(result, chunk)
		}
	}

	return result
}

func (m *RawStorage) SetChunkOrder(chunkOrder storage.ChunkOrder) {
	m.Called(chunkOrder)
	m.ChunkOrder = chunkOrder
}
