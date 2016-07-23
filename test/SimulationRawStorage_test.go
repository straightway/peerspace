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
	"github.com/straightway/straightway/simulation"
	"github.com/stretchr/testify/suite"
)

type SimulationRawStorage_Test struct {
	suite.Suite
	sut *simulation.RawStorage
}

func TestRawStorage(t *testing.T) {
	suite.Run(t, new(SimulationRawStorage_Test))
}

func (suite *SimulationRawStorage_Test) SetupTest() {
	suite.sut = &simulation.RawStorage{}
}

func (suite *SimulationRawStorage_Test) TearDownTest() {
	suite.sut = nil
}

// Tests

func (suite *SimulationRawStorage_Test) Test_CreateChunk_HasGivenKey() {
	chunk := suite.sut.CreateChunk(untimedKey, 0xffffffff)
	suite.Assert().Equal(untimedKey, chunk.Key)
}

func (suite *SimulationRawStorage_Test) Test_CreateChunk_HasGivenVirtualSize() {
	var definedSize uint32 = 1234
	chunk := suite.sut.CreateChunk(untimedKey, definedSize)
	actSize := suite.sut.SizeOf(chunk)
	suite.Assert().Equal(definedSize, actSize)
}

func (suite *SimulationRawStorage_Test) Test_GetSize_PanicsOnInvalidChunk() {
	chunk := &data.Chunk{Key: untimedKey, Data: []byte{1, 2}}
	suite.Assert().Panics(func() { suite.sut.SizeOf(chunk) })
}
