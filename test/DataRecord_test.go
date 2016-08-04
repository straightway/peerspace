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

	"github.com/straightway/straightway/data"
	"github.com/stretchr/testify/suite"
)

// Test suite

type DataRecord_Test struct {
	suite.Suite
}

func TestDataRecord(t *testing.T) {
	suite.Run(t, new(DataRecord_Test))
}

func (suite *DataRecord_Test) Test_ToChunkSlice_Empty() {
	results := data.SelectChunks(make([]data.Record, 0))
	suite.Assert().Empty(results)
}

func (suite *DataRecord_Test) Test_ToChunkSlice_NonEmpty() {
	record1 := data.Record{Chunk: &untimedChunk}
	record2 := data.Record{Chunk: &timedChunk10}
	results := data.SelectChunks([]data.Record{record1, record2})
	suite.Assert().Equal([]*data.Chunk{record1.Chunk, record2.Chunk}, results)
}
