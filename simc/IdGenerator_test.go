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

package simc

import (
	"math/rand"
	"testing"

	"github.com/stretchr/testify/suite"
)

type IdGenerator_Test struct {
	suite.Suite
	sut *IdGenerator
}

func TestIdGenerator(t *testing.T) {
	suite.Run(t, new(IdGenerator_Test))
}

func (suite *IdGenerator_Test) SetupTest() {
	suite.sut = &IdGenerator{RandSource: rand.NewSource(12345)}
}

func (suite *IdGenerator_Test) TearDownTest() {
	suite.sut = nil
}

// Tests

func (suite *IdGenerator_Test) Test_NextId_GeneratesNonEmptyId() {
	suite.Assert().NotEmpty(suite.sut.NextId())
}

func (suite *IdGenerator_Test) Test_NextId_GeneratesUniqueIds() {
	lastId := ""
	for i := 0; i < 10; i++ {
		currId := suite.sut.NextId()
		suite.Assert().NotEqual(lastId, currId)
		lastId = currId
	}
}
