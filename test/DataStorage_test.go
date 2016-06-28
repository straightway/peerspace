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

	"github.com/straightway/straightway/mocked"
	"github.com/straightway/straightway/peer"
	"github.com/straightway/straightway/storage"
	"github.com/stretchr/testify/suite"
)

// Test suite

type DataStorage_Test struct {
	suite.Suite
	sut *storage.Data
	raw *mocked.RawStorage
}

func TestDataStorage(t *testing.T) {
	suite.Run(t, new(DataStorage_Test))
}

func (suite *DataStorage_Test) SetupTest() {
	suite.raw = mocked.NewRawStorage()
	suite.sut = &storage.Data{RawStorage: suite.raw}
}

func (suite *DataStorage_Test) TearDownTest() {
	suite.sut = nil
	suite.raw = nil
}

// Tests

func (suite *DataStorage_Test) TestStoredChunkIsForwardedToRawStorage() {
	suite.sut.ConsiderStorage(&untimedChunk)
	suite.raw.AssertCalledOnce(suite.T(), "Store", &untimedChunk)
}

func (suite *DataStorage_Test) TestQueryIsForwardedToRawStorage() {
	suite.raw.Store(&untimedChunk)
	query := peer.Query{Id: queryId}
	result := suite.sut.Query(query)
	suite.raw.AssertCalledOnce(suite.T(), "Query", query)
	suite.Assert().Equal(suite.raw.Query(query), result)
}
