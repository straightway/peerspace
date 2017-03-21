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
	"testing"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/times"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type Storage_Test struct {
	Storage_TestBase
}

func TestDataStorage(t *testing.T) {
	suite.Run(t, new(Storage_Test))
}

// Tests

func (suite *Storage_Test) Test_Query_IsForwardedToRawStorage() {
	suite.raw.Store(&data.UntimedChunk, 0.0, times.Max())
	query := data.Query{Id: data.QueryId}
	result := suite.sut.Query(query)
	suite.raw.AssertCalledOnce(suite.T(), "Query", query)
	suite.Assert().Equal([]*data.Chunk{&data.UntimedChunk}, result)
}

func (suite *Storage_Test) Test_Query_LeadsToRePrioritizationOfResult() {
	suite.raw.Store(&data.UntimedChunk, -10.0, time.Unix(0, 0).In(time.UTC))
	query := data.Query{Id: data.QueryId}
	result := suite.sut.Query(query)
	suite.raw.AssertCalledOnce(suite.T(), "RePrioritize", data.UntimedChunk.Key, float32(0.0), times.Max())
	suite.Assert().Equal([]*data.Chunk{&data.UntimedChunk}, result)
}

func (suite *Storage_Test) Test_IsStored_ForwardsUntimedQueryToRawStorage() {
	result := suite.sut.IsStored(data.UntimedKey)
	suite.raw.AssertCalledOnce(suite.T(), "Query", data.Query{Id: data.UntimedKey.Id})
	suite.Assert().False(result)
}

func (suite *Storage_Test) Test_IsStored_ForwardsTimedQueryToRawStorage() {
	result := suite.sut.IsStored(data.TimedKey10)
	suite.raw.OnNew("query")
	suite.raw.AssertCalledOnce(
		suite.T(),
		"Query",
		data.Query{Id: data.TimedKey10.Id, TimeFrom: data.TimedKey10.TimeStamp, TimeTo: data.TimedKey10.TimeStamp})
	suite.Assert().False(result)
}

func (suite *Storage_Test) Test_IsStored_ForwardsYieldsTrueIfRawStorageYieldsResult() {
	suite.raw.Store(&data.UntimedChunk, 0.0, time.Now())
	result := suite.sut.IsStored(data.UntimedKey)
	suite.Assert().True(result)
}

func (suite *Storage_Test) Test_ConsiderStorage_IsForwardedToRawStorage() {
	suite.sut.ConsiderStorage(&data.UntimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &data.UntimedChunk, mock.Anything, mock.Anything)
}

func (suite *Storage_Test) Test_ConsiderStorage_OverridesItemIfSameSizeEvenIfRawStorageIsFull() {
	suite.raw.Store(&data.UntimedChunk, 10.0, times.Max())
	suite.raw.CurrentFreeStorage = 0
	suite.raw.Calls = nil
	suite.sut.ConsiderStorage(&data.UntimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &data.UntimedChunk, mock.Anything, mock.Anything)
}

func (suite *Storage_Test) Test_ConsiderStorage_StoresNotIfItemWithSameKeyIsBiggerAndNoStorageSpaceLeft() {
	suite.raw.Store(&data.UntimedChunk, 10.0, times.Max())
	suite.raw.CurrentFreeStorage = 0
	suite.raw.Calls = nil
	biggerChunk := data.Chunk{Key: data.UntimedChunk.Key, Data: make([]byte, len(data.UntimedChunk.Data)+1, len(data.UntimedChunk.Data)+1)}
	suite.sut.ConsiderStorage(&biggerChunk)

	suite.raw.AssertNotCalled(suite.T(), "Store", mock.Anything, mock.Anything, mock.Anything)
	suite.Assert().Equal(&data.UntimedChunk, suite.raw.Data[0].Chunk)
}

func (suite *Storage_Test) Test_ConsiderStorage_UsesPriorityGeneratorToDeterminePriority() {
	priorityGenerator := data.NewPriorityGeneratorMock(2.0, times.Max())
	suite.sut.PriorityGenerator = priorityGenerator
	suite.sut.ConsiderStorage(&data.UntimedChunk)
	priorityGenerator.AssertCalledOnce(suite.T(), "Priority", &data.UntimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &data.UntimedChunk, float32(2.0), mock.Anything)
}

func (suite *Storage_Test) Test_Startup_IsForwardedToRawStorage() {
	suite.sut.Startup()
	suite.raw.AssertCalledOnce(suite.T(), "Startup")
}

func (suite *Storage_Test) Test_Startup_IsIgnoredIfNotSupportedByRawStorage() {
	suite.sut.RawStorage = data.NewRawStorageMock(suite.timer)
	suite.Assert().NotPanics(func() { suite.sut.Startup() })
}

func (suite *Storage_Test) Test_ShutDown_IsForwardedToRawStorage() {
	suite.sut.ShutDown()
	suite.raw.AssertCalledOnce(suite.T(), "ShutDown")
}

func (suite *Storage_Test) Test_Shutdown_IsIgnoredIfNotSupportedByRawStorage() {
	suite.sut.RawStorage = data.NewRawStorageMock(suite.timer)
	suite.Assert().NotPanics(func() { suite.sut.ShutDown() })
}
