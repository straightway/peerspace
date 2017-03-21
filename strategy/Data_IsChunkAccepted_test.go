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

package strategy

import (
	"testing"

	"github.com/straightway/straightway/data"
	"github.com/stretchr/testify/suite"
)

type Data_IsChunkAccepted_Test struct {
	Data_TestBase
}

func TestDataStrategy_IsChunkAccepted(t *testing.T) {
	suite.Run(t, new(Data_IsChunkAccepted_Test))
}

// Tests

func (suite *Data_IsChunkAccepted_Test) TestChunkSmallerThanMaxChunkSizeIsAccepted() {
	suite.configuration.MaxChunkSize = 5
	chunk := &data.Chunk{Data: make([]byte, 5, 5)}
	suite.Assert().True(suite.sut.IsChunkAccepted(chunk, nil))
}

func (suite *Data_IsChunkAccepted_Test) TestChunkBiggerThanMaxChunkSizeIsRejected() {
	suite.configuration.MaxChunkSize = 5
	chunk := &data.Chunk{Data: make([]byte, 6, 6)}
	suite.Assert().False(suite.sut.IsChunkAccepted(chunk, nil))
}
