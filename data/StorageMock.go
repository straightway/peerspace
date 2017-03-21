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
	"github.com/stretchr/testify/mock"

	"github.com/straightway/straightway/general/mocked"
)

type StorageMock struct {
	mocked.Base
	data         []*Chunk
	StartupCalls int
}

func NewStorageMock(result ...*Chunk) *StorageMock {
	dataStorage := &StorageMock{data: make([]*Chunk, 0)}
	dataStorage.On("ConsiderStorage", mock.Anything).Return()
	for _, chunk := range result {
		dataStorage.ConsiderStorage(chunk)
	}

	dataStorage.On("Query", mock.Anything).Return()
	dataStorage.On("Startup").Return()
	dataStorage.On("ShutDown").Return()
	dataStorage.On("IsStored", mock.Anything).Return()

	return dataStorage
}

func (this *StorageMock) ConsiderStorage(data *Chunk) {
	this.Called(data)
	if data != nil {
		this.data = append(this.data, data)
	}
}

func (this *StorageMock) Query(query Query) []*Chunk {
	this.Called(query)
	result := make([]*Chunk, 0)
	for _, chunk := range this.data {
		if query.Matches(chunk.Key) {
			result = append(result, chunk)
		}
	}

	return result
}

func (this *StorageMock) IsStored(key Key) bool {
	this.Called()
	for _, chunk := range this.data {
		if chunk.Key == key {
			return true
		}
	}

	return false
}

func (this *StorageMock) Startup() {
	this.Called()
	this.StartupCalls++
}

func (this *StorageMock) ShutDown() {
	this.Called()
	this.StartupCalls++
}
