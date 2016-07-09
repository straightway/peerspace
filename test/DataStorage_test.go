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
	"math"
	"testing"
	"time"

	"github.com/straightway/straightway/data"
	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
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
	suite.raw.Store(&untimedChunk, 0.0, time.Unix(math.MaxInt64, 0))
	query := peer.Query{Id: queryId}
	result := suite.sut.Query(query)
	suite.raw.AssertCalledOnce(suite.T(), "Query", query)
	suite.Assert().Equal([]*data.Chunk{&untimedChunk}, result)
}

func (suite *DataStorage_Test) Test_ConsiderStorage_IsForwardedToRawStorage() {
	suite.sut.ConsiderStorage(&untimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &untimedChunk, mock.Anything, mock.Anything)
}

func (suite *DataStorage_Test) Test_ConsiderStorage_UsesPriorityGeneratorToDeterminePriority() {
	priorityGenerator := mocked.NewPriorityGenerator(2.0, time.Unix(math.MaxInt64, 0))
	suite.sut.PriorityGenerator = priorityGenerator
	suite.sut.ConsiderStorage(&untimedChunk)
	priorityGenerator.AssertCalledOnce(suite.T(), "Priority", &untimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &untimedChunk, float32(2.0), mock.Anything)
}
