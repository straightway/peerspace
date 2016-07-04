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
	"github.com/stretchr/testify/mock"
)

type DataStorage struct {
	Base
	data         []*data.Chunk
	StartupCalls int
}

func NewDataStorage(result ...*data.Chunk) *DataStorage {
	dataStorage := &DataStorage{data: make([]*data.Chunk, 0)}
	dataStorage.On("ConsiderStorage", mock.Anything).Return()
	for _, chunk := range result {
		dataStorage.ConsiderStorage(chunk)
	}

	dataStorage.On("Query", mock.Anything).Return()
	dataStorage.On("Startup").Return()

	return dataStorage
}

func (this *DataStorage) ConsiderStorage(data *data.Chunk) {
	this.Called(data)
	if data != nil {
		this.data = append(this.data, data)
	}
}

func (this *DataStorage) Query(query peer.Query) []*data.Chunk {
	this.Called(query)
	result := make([]*data.Chunk, 0)
	for _, chunk := range this.data {
		if query.Matches(chunk.Key) {
			result = append(result, chunk)
		}
	}

	return result
}

func (this *DataStorage) Startup() {
	this.Called()
	this.StartupCalls++
}
