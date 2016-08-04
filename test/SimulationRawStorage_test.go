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

package test

import (
	"testing"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/loop"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/simc"
	"github.com/straightway/straightway/storage"
	"github.com/stretchr/testify/suite"
)

type SimulationRawStorage_Test struct {
	suite.Suite
	sut   *simc.RawStorage
	timer *mocked.Timer
}

const (
	initialFreeStorage = uint64(1000)
	chunkSize          = uint64(12)
)

func TestRawStorage(t *testing.T) {
	suite.Run(t, new(SimulationRawStorage_Test))
}

func (suite *SimulationRawStorage_Test) SetupTest() {
	suite.timer = &mocked.Timer{}
	suite.sut = &simc.RawStorage{
		FreeStorageValue: initialFreeStorage,
		Timer:            suite.timer}
}

func (suite *SimulationRawStorage_Test) TearDownTest() {
	suite.timer = nil
	suite.sut = nil
}

// Tests

func (suite *SimulationRawStorage_Test) Test_CreateChunk_HasGivenKey() {
	chunk := suite.sut.CreateChunk(untimedKey, 0xffffffff)
	suite.Assert().Equal(untimedKey, chunk.Key)
}

func (suite *SimulationRawStorage_Test) Test_CreateChunk_HasGivenVirtualSize() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	actSize := suite.sut.SizeOf(chunk)
	suite.Assert().Equal(chunkSize, actSize)
}

func (suite *SimulationRawStorage_Test) Test_GetSize_PanicsOnInvalidChunk() {
	chunk := &data.Chunk{Key: untimedKey, Data: []byte{1, 2}}
	suite.Assert().Panics(func() { suite.sut.SizeOf(chunk) })
}

func (suite *SimulationRawStorage_Test) Test_FreeStorage_YieldsSpecifiedValue() {
	suite.Assert().Equal(initialFreeStorage, suite.sut.FreeStorage())
}

func (suite *SimulationRawStorage_Test) Test_Store_ReducesFreeStorage() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	suite.Assert().Equal(initialFreeStorage-chunkSize, suite.sut.FreeStorage())
}

func (suite *SimulationRawStorage_Test) Test_Store_CanBeRetrievedAgain() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	result := suite.leastImportantData()
	suite.Assert().Equal([]*data.Chunk{chunk}, result)
}

func (suite *SimulationRawStorage_Test) Test_Store_CanBeOverridden() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	expirationTime := time.Unix(10, 0).In(time.UTC)
	suite.sut.Store(chunk, 1.0, expirationTime)
	queryResult := suite.sut.Query(data.Query{Id: untimedKey.Id})
	suite.Assert().Equal(
		[]storage.DataRecord{storage.DataRecord{Chunk: chunk, Priority: 1.0, PrioExpirationTime: expirationTime}},
		queryResult)
}

func (suite *SimulationRawStorage_Test) Test_LeastImportantData_YieldsDataSortedByPriority() {
	chunk2 := suite.sut.CreateChunk(timedKey10, chunkSize)
	suite.sut.Store(chunk2, 1.0, time.Time{})
	chunk1 := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk1, 0.0, time.Time{})
	result := suite.leastImportantData()
	suite.Assert().Equal([]*data.Chunk{chunk1, chunk2}, result)
}

func (suite *SimulationRawStorage_Test) Test_Delete_StoredData() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	suite.sut.Delete(untimedKey)
	suite.Assert().Empty(suite.leastImportantData())
}

func (suite *SimulationRawStorage_Test) Test_Delete_FreesStorage() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	suite.sut.Delete(untimedKey)
	suite.Assert().Equal(initialFreeStorage, suite.sut.FreeStorage())
}

func (suite *SimulationRawStorage_Test) Test_Query_UntimedReturnsSingleResult() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	otherChunk := suite.sut.CreateChunk(timedKey10, chunkSize)
	suite.sut.Store(otherChunk, 0.0, time.Time{})
	queryResult := suite.sut.Query(data.Query{Id: untimedKey.Id})
	suite.Assert().Equal([]storage.DataRecord{storage.DataRecord{Chunk: chunk}}, queryResult)
}

func (suite *SimulationRawStorage_Test) Test_RePrioritize_PanicsIfDataNotPresent() {
	suite.Assert().Panics(func() { suite.sut.RePrioritize(untimedKey, 0.0, time.Time{}) })
}

func (suite *SimulationRawStorage_Test) Test_RePrioritize_AssignsNewValues() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Time{})
	expirationTime := time.Unix(10, 0).In(time.UTC)
	suite.sut.RePrioritize(untimedKey, 1.0, expirationTime)
	queryResult := suite.sut.Query(data.Query{Id: untimedKey.Id})
	suite.Assert().Equal(
		[]storage.DataRecord{storage.DataRecord{Chunk: chunk, Priority: 1.0, PrioExpirationTime: expirationTime}},
		queryResult)
}

func (suite *SimulationRawStorage_Test) Test_ExpiredData_IsEmptyIfNoDataIsStored() {
	suite.Assert().Empty(suite.sut.ExpiredData())
}

func (suite *SimulationRawStorage_Test) Test_ExpiredData_IsEmptyIfNoDataIsExpired() {
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, time.Unix(10, 0).In(time.UTC))
	suite.Assert().Empty(suite.sut.ExpiredData())
}

func (suite *SimulationRawStorage_Test) Test_ExpiredData_YieldsExpiredData() {
	expirationTime := time.Unix(10, 0).In(time.UTC)
	chunk := suite.sut.CreateChunk(untimedKey, chunkSize)
	suite.sut.Store(chunk, 0.0, expirationTime)
	suite.timer.CurrentTime = expirationTime
	suite.Assert().Equal(
		[]storage.DataRecord{storage.DataRecord{Chunk: chunk, Priority: 0.0, PrioExpirationTime: expirationTime}},
		suite.sut.ExpiredData())
}

// Private

func (suite *SimulationRawStorage_Test) leastImportantData() []*data.Chunk {
	result := make([]*data.Chunk, 0, 0)
	suite.sut.LeastImportantData().Loop(func(item interface{}) loop.Control {
		result = append(result, item.(*data.Chunk))
		return loop.Continue
	})
	return result
}
