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
	"math/rand"
	"testing"

	"github.com/straightway/straightway/sim_"
	"github.com/stretchr/testify/suite"
)

type SimulationIdGenerator_Test struct {
	suite.Suite
	sut *sim_.IdGenerator
}

func TestSimulationIdGenerator(t *testing.T) {
	suite.Run(t, new(SimulationIdGenerator_Test))
}

func (suite *SimulationIdGenerator_Test) SetupTest() {
	suite.sut = &sim_.IdGenerator{RandSource: rand.NewSource(12345)}
}

func (suite *SimulationIdGenerator_Test) TearDownTest() {
	suite.sut = nil
}

// Tests

func (suite *SimulationIdGenerator_Test) Test_NextId_GeneratesNonEmptyId() {
	suite.Assert().NotEmpty(suite.sut.NextId())
}

func (suite *SimulationIdGenerator_Test) Test_NextId_GeneratesUniqueIds() {
	lastId := ""
	for i := 0; i < 10; i++ {
		currId := suite.sut.NextId()
		suite.Assert().NotEqual(lastId, currId)
		lastId = currId
	}
}
