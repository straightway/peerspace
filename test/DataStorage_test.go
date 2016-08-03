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
	"github.com/straightway/straightway/general"
	"github.com/straightway/straightway/mocked"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type DataStorage_Test struct {
	DataStorage_TestBase
}

func TestDataStorage(t *testing.T) {
	suite.Run(t, new(DataStorage_Test))
}

// Tests

func (suite *DataStorage_Test) Test_Query_IsForwardedToRawStorage() {
	suite.raw.Store(&untimedChunk, 0.0, general.MaxTime())
	query := data.Query{Id: queryId}
	result := suite.sut.Query(query)
	suite.raw.AssertCalledOnce(suite.T(), "Query", query)
	suite.Assert().Equal([]*data.Chunk{&untimedChunk}, result)
}

func (suite *DataStorage_Test) Test_Query_LeadsToRePrioritizationOfResult() {
	suite.raw.Store(&untimedChunk, -10.0, time.Unix(0, 0).In(time.UTC))
	query := data.Query{Id: queryId}
	result := suite.sut.Query(query)
	suite.raw.AssertCalledOnce(suite.T(), "RePrioritize", untimedChunk.Key, float32(0.0), general.MaxTime())
	suite.Assert().Equal([]*data.Chunk{&untimedChunk}, result)
}

func (suite *DataStorage_Test) Test_ConsiderStorage_IsForwardedToRawStorage() {
	suite.sut.ConsiderStorage(&untimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &untimedChunk, mock.Anything, mock.Anything)
}

func (suite *DataStorage_Test) Test_ConsiderStorage_OverridesItemIfSameSizeEvenIfRawStorageIsFull() {
	suite.raw.Store(&untimedChunk, 10.0, general.MaxTime())
	suite.raw.CurrentFreeStorage = 0
	suite.raw.Calls = nil
	suite.sut.ConsiderStorage(&untimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &untimedChunk, mock.Anything, mock.Anything)
}

func (suite *DataStorage_Test) Test_ConsiderStorage_StoresNotIfItemWithSameKeyIsBiggerAndNoStorageSpaceLeft() {
	suite.raw.Store(&untimedChunk, 10.0, general.MaxTime())
	suite.raw.CurrentFreeStorage = 0
	suite.raw.Calls = nil
	biggerChunk := data.Chunk{Key: untimedChunk.Key, Data: make([]byte, len(untimedChunk.Data)+1, len(untimedChunk.Data)+1)}
	suite.sut.ConsiderStorage(&biggerChunk)

	suite.raw.AssertNotCalled(suite.T(), "Store", mock.Anything, mock.Anything, mock.Anything)
	suite.Assert().Equal(&untimedChunk, suite.raw.Data[0].Chunk)
}

func (suite *DataStorage_Test) Test_ConsiderStorage_UsesPriorityGeneratorToDeterminePriority() {
	priorityGenerator := mocked.NewPriorityGenerator(2.0, general.MaxTime())
	suite.sut.PriorityGenerator = priorityGenerator
	suite.sut.ConsiderStorage(&untimedChunk)
	priorityGenerator.AssertCalledOnce(suite.T(), "Priority", &untimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &untimedChunk, float32(2.0), mock.Anything)
}

func (suite *DataStorage_Test) Test_Startup_IsForwardedToRawStorage() {
	suite.sut.Startup()
	suite.raw.AssertCalledOnce(suite.T(), "Startup")
}

func (suite *DataStorage_Test) Test_Startup_IsIgnoredIfNotSupportedByRawStorage() {
	suite.sut.RawStorage = mocked.NewRawStorage(suite.timer)
	suite.Assert().NotPanics(func() { suite.sut.Startup() })
}

func (suite *DataStorage_Test) Test_ShutDown_IsForwardedToRawStorage() {
	suite.sut.ShutDown()
	suite.raw.AssertCalledOnce(suite.T(), "ShutDown")
}

func (suite *DataStorage_Test) Test_Shutdown_IsIgnoredIfNotSupportedByRawStorage() {
	suite.sut.RawStorage = mocked.NewRawStorage(suite.timer)
	suite.Assert().NotPanics(func() { suite.sut.ShutDown() })
}
