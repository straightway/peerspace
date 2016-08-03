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
	"fmt"
	"testing"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/general/times"
	"github.com/straightway/straightway/mocked"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/suite"
)

// Test suite

type DataStorage_Cleanup_Test struct {
	DataStorage_TestBase
}

func TestDataStorage_Cleanup(t *testing.T) {
	suite.Run(t, new(DataStorage_Cleanup_Test))
}

// Tests

func (suite *DataStorage_Cleanup_Test) Test_DeleteNoChunksIfEnoughSpaceIsFree() {
	suite.createTestChunksOfSize(10, 1)
	chunkToAdd := createTestChunkOfSize(11)
	suite.raw.CurrentFreeStorage = 11

	suite.sut.ConsiderStorage(chunkToAdd)

	suite.raw.AssertNotCalled(suite.T(), "Delete", mock.Anything)
	suite.raw.AssertCalledOnce(suite.T(), "Store", chunkToAdd, mock.Anything, mock.Anything)
}

func (suite *DataStorage_Cleanup_Test) Test_RefuseStorageIfChunkIsTooBig() {
	chunkToAdd := createTestChunkOfSize(10)
	suite.raw.CurrentFreeStorage = 9

	suite.sut.ConsiderStorage(chunkToAdd)

	suite.raw.AssertNotCalled(suite.T(), "Delete", mock.Anything)
	suite.raw.AssertNotCalled(suite.T(), "Store", mock.Anything)
}

func (suite *DataStorage_Cleanup_Test) Test_DeleteLeastImportantChunkIfNoSpaceLeft() {
	chunkToCleanUp := suite.createTestChunksOfSize(10, 2)[0]
	chunkToAdd := createTestChunkOfSize(10)
	suite.raw.CurrentFreeStorage = 0

	suite.sut.ConsiderStorage(chunkToAdd)

	suite.raw.AssertCalledOnce(suite.T(), "Delete", chunkToCleanUp.Key)
	suite.raw.AssertCalledOnce(suite.T(), "Store", chunkToAdd, mock.Anything, mock.Anything)
}

func (suite *DataStorage_Cleanup_Test) Test_DeleteSeveralChunksIfNeeded() {
	chunksToCleanUp := suite.createTestChunksOfSize(10, 2)
	chunkToAdd := createTestChunkOfSize(11)
	suite.raw.CurrentFreeStorage = 0

	suite.sut.ConsiderStorage(chunkToAdd)

	suite.raw.AssertCalled(suite.T(), "Delete", chunksToCleanUp[0].Key)
	suite.raw.AssertCalled(suite.T(), "Delete", chunksToCleanUp[1].Key)
	suite.raw.AssertCalledOnce(suite.T(), "Store", chunkToAdd, mock.Anything, mock.Anything)
}

func (suite *DataStorage_Cleanup_Test) Test_DeleteNoChunksIfNoChunkIsBiggerThanOverallStorageSize() {
	suite.createTestChunksOfSize(10, 2)
	chunkToAdd := createTestChunkOfSize(21)
	suite.raw.CurrentFreeStorage = 0

	suite.sut.ConsiderStorage(chunkToAdd)

	suite.raw.AssertNotCalled(suite.T(), "Delete", mock.Anything)
	suite.raw.AssertNotCalled(suite.T(), "Store", mock.Anything)
}

func (suite *DataStorage_Cleanup_Test) Test_RePrioritizeExpiredChunksBeforeCleanup() {
	suite.createTestChunksOfSize(10, 2)

	chunkToRePrioritize := createTestChunkOfSize(10)
	suite.raw.Store(chunkToRePrioritize, -10.0, time.Unix(10, 0).In(time.UTC))

	suite.timer.CurrentTime = time.Unix(11, 0).In(time.UTC)

	const newPrio = float32(1000.0)
	suite.sut.PriorityGenerator = mocked.NewPriorityGenerator(newPrio, times.Max())

	chunkToAdd := createTestChunkOfSize(10)
	suite.raw.CurrentFreeStorage = 0
	suite.raw.Calls = nil

	suite.sut.ConsiderStorage(chunkToAdd)

	suite.raw.AssertNotCalled(suite.T(), "Delete", chunkToRePrioritize.Key)
	suite.raw.AssertCalledOnce(suite.T(), "RePrioritize", chunkToRePrioritize.Key, newPrio, times.Max())
	suite.raw.AssertCalledOnce(suite.T(), "Store", chunkToAdd, newPrio, times.Max())
}

// Private

var nextChunkId = 0

func createTestChunkOfSize(size int) *data.Chunk {
	nextChunkId++
	id := fmt.Sprintf("%v", nextChunkId)
	return &data.Chunk{Key: data.Key{Id: id}, Data: make([]byte, size, size)}
}

func (suite *DataStorage_Cleanup_Test) createTestChunksOfSize(size, number int) (result []*data.Chunk) {
	result = make([]*data.Chunk, size, size)
	for i := number - 1; 0 <= i; i-- {
		result[i] = createTestChunkOfSize(size)
		suite.raw.Store(result[i], float32(i+1)*10.0, times.Max())
	}

	suite.raw.Calls = nil

	return
}
