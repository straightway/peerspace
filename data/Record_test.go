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
	"sort"
	"testing"

	"github.com/stretchr/testify/suite"
)

// Test suite

type Record_Test struct {
	suite.Suite
}

func TestDataRecord(t *testing.T) {
	suite.Run(t, new(Record_Test))
}

// Tests

func (suite *Record_Test) Test_ToChunkSlice_Empty() {
	results := SelectChunks(make([]Record, 0))
	suite.Assert().Empty(results)
}

func (suite *Record_Test) Test_ToChunkSlice_NonEmpty() {
	record1 := Record{Chunk: &UntimedChunk}
	record2 := Record{Chunk: &TimedChunk10}
	results := SelectChunks([]Record{record1, record2})
	suite.Assert().Equal([]*Chunk{record1.Chunk, record2.Chunk}, results)
}

func (suite *Record_Test) Test_Sorting() {
	toSort := []Record{
		Record{Priority: 0.0},
		Record{Priority: 3.0},
		Record{Priority: 1.0},
		Record{Priority: 2.0}}
	expectedResult := []Record{
		Record{Priority: 0.0},
		Record{Priority: 1.0},
		Record{Priority: 2.0},
		Record{Priority: 3.0}}
	sort.Sort(RecordByPriority(toSort))
	suite.Assert().Equal(expectedResult, toSort)
}
